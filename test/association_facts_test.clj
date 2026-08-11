(ns association-facts-test
  (:require [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [clojure.test :refer [deftest is testing]]
            [kotoba.compiler.core :as compiler]
            [kotoba.kir :as ir]))

(def source (slurp "src/association_facts.kotoba"))
(defn call [kir function & args] (ir/execute kir function (vec args)))
(defn present [option] (when (second option) (nth option 2)))

(def fields
  ["id" "title" "association" "isic" "country" "kind" "url"
   "url-provenance" "established-date" "retrieved-at"])

(def expected
  [{"id" "gapki.founding-1981"
    "title" "GAPKI founding (GAPKI History)"
    "association" "gapki"
    "isic" "0126"
    "country" "IDN"
    "kind" "governance-program"
    "url" "https://gapki.id/en/gapki-history/"
    "url-provenance" "official-gapki-id"
    "established-date" "1981-02-27"
    "retrieved-at" "2026-07-16"}
   {"id" "gapki.45-tahun-gapki-book-2026"
    "title" "'45 Tahun GAPKI untuk Negeri' history book presented at 45th anniversary"
    "association" "gapki"
    "isic" "0126"
    "country" "IDN"
    "kind" "governance-program"
    "url" "https://gapki.id/en/news/2026/05/05/gapki-releases-palm-history-book-recounting-ris-catch-up-with-malaysia/"
    "url-provenance" "official-gapki-id"
    "established-date" "2026-04-29"
    "retrieved-at" "2026-07-16"}])

(deftest reference-preserves-the-complete-catalog
  (let [kir (:kir (compiler/compile-source source :js-kotoba-v1))
        observed (mapv (fn [index]
                         (into {} (map (fn [field]
                                         [field (present (call kir 'entry-field
                                                              "gapki" index field))])
                                       fields)))
                       (range (call kir 'entry-count "gapki")))]
    (is (= expected observed))
    (is (true? (call kir 'association-covered? "gapki")))
    (is (false? (call kir 'association-covered? "vnba")))
    (is (= [1 1] (mapv #(call kir 'topic-count "gapki" %) [0 1])))
    (is (= ["governance" "governance"]
           (mapv #(present (call kir 'topic "gapki" % 0)) [0 1])))
    (is (= 2 (call kir 'by-topic-count "gapki" "governance")))
    (is (= (mapv #(get % "id") expected)
           (mapv #(present (call kir 'by-topic-id "gapki" "governance" %))
                 [0 1])))
    (is (= #{} (set (:effects kir))))
    (testing "unknown association, field, topic, and indexes fail closed"
      (is (zero? (call kir 'entry-count "vnba")))
      (is (nil? (present (call kir 'entry-field "vnba" 0 "id"))))
      (is (nil? (present (call kir 'entry-field "gapki" -1 "id"))))
      (is (nil? (present (call kir 'entry-field "gapki" 2 "id"))))
      (is (nil? (present (call kir 'entry-field "gapki" 0 "unknown"))))
      (is (nil? (present (call kir 'topic "gapki" 0 1))))
      (is (zero? (call kir 'by-topic-count "gapki" "labor")))
      (is (nil? (present (call kir 'by-topic-id "gapki" "labor" 0)))))))

(defn compiler-root []
  (nth (iterate #(.getParent ^java.nio.file.Path %)
                (java.nio.file.Path/of (.toURI (io/resource "kotoba/compiler/core.clj")))) 4))
(defn base64 [value] (.encodeToString (java.util.Base64/getEncoder) value))

(deftest restricted-javascript-and-typed-wasm-conform-semantically
  (let [javascript (compiler/compile-source source :js-kotoba-v1)
        wasm (compiler/compile-source source :wasm32-browser-kotoba-v1)
        js64 (base64 (.getBytes ^String (:source javascript) "UTF-8"))
        wasm64 (base64 ^bytes (:bytes wasm))
        probe
        (shell/sh
          "node" "--input-type=module" "-e"
          (str "import(process.argv[1]).then(async host=>{"
               "const j=await import('data:text/javascript;base64," js64 "');"
               "const w=await host.instantiateKotoba(Buffer.from(process.argv[2],'base64'));"
               "const run=x=>{"
               "if(x['association-covered?']('gapki')!==true||x['association-covered?']('vnba')!==false)throw Error('covered');"
               "if(x['entry-count']('gapki')!==2n||x['entry-count']('vnba')!==0n)throw Error('count');"
               "if(x['entry-field']('gapki',0n,'id')[2]!=='gapki.founding-1981')throw Error('first');"
               "if(x['entry-field']('gapki',1n,'established-date')[2]!=='2026-04-29')throw Error('second');"
               "if(x['topic']('gapki',1n,0n)[2]!=='governance'||x['by-topic-count']('gapki','governance')!==2n)throw Error('topic');"
               "if(x['entry-field']('gapki',2n,'id')[1]!==false||x['entry-field']('gapki',0n,'unknown')[1]!==false)throw Error('reject');"
               "};run(j.instantiateKotoba({}));run(w.instance.exports);"
               "}).catch(e=>{console.error(e);process.exit(99)})")
          (.toString (.toUri (.resolve (compiler-root) "runtime/browser-host.mjs"))) wasm64)]
    (is (zero? (:exit probe)) (str (:out probe) (:err probe)))))

(deftest production-source-authority
  (is (= ["src/association_facts.kotoba"]
         (->> (file-seq (io/file "src")) (filter #(.isFile %)) (map str) sort vec))))
