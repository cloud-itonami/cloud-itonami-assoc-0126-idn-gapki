(ns association.facts
  "Industry rule/history catalog for the Indonesian Palm Oil
  Association (GAPKI, Gabungan Pengusaha Kelapa Sawit Indonesia) --
  a 36th industry-association-level source per ADR-2607141700
  (cloud-itonami-compliance-fact-federation). The FIRST entry aligned
  to ISIC 0126 (growing of oleaginous fruits), a new industry code
  for this family. Continues this session's Indonesia research
  thread alongside cloud-itonami-municipality-idn-jakarta.

  Both entries directly WebFetch-verified against gapki.id (the
  association's own official domain): the History page states
  plainly 'The Indonesian Palm Oil Association (GAPKI) was
  established on 27 February 1981'; a news article describes the
  presentation of '45 Tahun GAPKI untuk Negeri' (45 Years of GAPKI
  for the Nation), a historical chronicle book, 'on Tuesday
  (29/04/2026)' at GAPKI's 45th-anniversary celebration in Jakarta.

  No Wikidata Q-id was found for GAPKI specifically -- omitted rather
  than guessed.

  A rule not in this table has NO spec-basis, full stop; extend
  `catalog`, do not invent an id/url.")

(def catalog
  "assoc-slug -> vector of self-regulatory rule entries."
  {"gapki"
   [{:association-rule/id "gapki.founding-1981"
     :association-rule/title "GAPKI founding (GAPKI History)"
     :association-rule/association "gapki"
     :association-rule/isic "0126"
     :association-rule/country "IDN"
     :association-rule/kind :governance-program
     :association-rule/url "https://gapki.id/en/gapki-history/"
     :association-rule/url-provenance :official-gapki-id
     :association-rule/established-date "1981-02-27"
     :association-rule/retrieved-at "2026-07-16"
     :association-rule/topic #{:governance}}
    {:association-rule/id "gapki.45-tahun-gapki-book-2026"
     :association-rule/title "'45 Tahun GAPKI untuk Negeri' history book presented at 45th anniversary"
     :association-rule/association "gapki"
     :association-rule/isic "0126"
     :association-rule/country "IDN"
     :association-rule/kind :governance-program
     :association-rule/url "https://gapki.id/en/news/2026/05/05/gapki-releases-palm-history-book-recounting-ris-catch-up-with-malaysia/"
     :association-rule/url-provenance :official-gapki-id
     :association-rule/established-date "2026-04-29"
     :association-rule/retrieved-at "2026-07-16"
     :association-rule/topic #{:governance}}]})

(defn spec-basis [assoc-slug] (get catalog assoc-slug))

(defn coverage
  ([] (coverage (keys catalog)))
  ([slugs]
   (let [have (filter catalog slugs)
         missing (remove catalog slugs)]
     {:requested (count slugs)
      :covered (count have)
      :covered-associations (vec (sort have))
      :missing-associations (vec (sort missing))
      :note (str "cloud-itonami-assoc-0126-idn-gapki Wave 0 (ADR-2607141700): "
                 (count (get catalog "gapki")) " gapki entries seeded with an "
                 "official gapki.id citation. Extend "
                 "`association.facts/catalog`, never fabricate a rule id/url.")})))

(defn by-topic [assoc-slug topic]
  (filterv #(contains? (:association-rule/topic %) topic) (spec-basis assoc-slug)))
