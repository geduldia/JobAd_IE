# JobAd_IE
Classification (Zone Analysis) and Information Extraction From Job Ads

Code for Classification and Information Extraction from job advertisements as part of my <a href="http://www.spinfo.phil-fak.uni-koeln.de/sites/spinfo/arbeiten/Masterthesis_Alena.pdf">Master's Thesis</a>.

### 1.Classification/Zone-Analysis
Splits JobAds into paragraphs and classifies them into the four classes
  1. company description
  2. job description
  3. applicants profile
  4. formalities
  
  ### 2. Information Extraction
  Extract competences from applicants profiles
  
  ____________________________________________________________________________________

Die Klassen und weiteren Dateien des Projekts sind in der folgenden Paketstruktur geordnet, welche die jeweilige Funktionalität wiederspiegeln soll:

![packages](https://cloud.githubusercontent.com/assets/4161405/24959361/dc254c58-1f92-11e7-8860-2b499882896f.PNG)
 
Sämtliche ausführbaren Klassen liegen als JUnit-Testklassen vor und stellen vollständige Workflows dar.

Mit classifyJobAdsIntoParagraphs kann eine Stellenanzeigen-Datenbank in Paragraphen der oben genannten Klassen klassifiziert werden. Die Ergebnisse werden als Datenbankfiles gespeichert (unter test/resources/classification/output).

SimpleRulebasedExraction verwendet diese als Input zur Kompetenzextraktion und speichert die Ergebnisse ebenfalls als Datenbankfile (test/resources/information_extraction/output).

Mit CreateCompetenceTrainingData, einem interaktiven Workflow zur Annotation von Kompetenzen, kann ein Testkorpus für Evaluationszwecke erstellt werden. (Ein manuell annotiertes Korpus befindet sich bereits im Ordern test/resources/information_extraction/trainingdata)

EvaluateSimpleRulebasedExtraction und EvaluateBootstrapExtraction, führen eine Extraktion mit dem jeweiligen Verfahren durch und evaluieren die Ergebnisse im Anschluss. Ausführliche Evaluationsergebnisse (inklusive aller richtig und falsch extrahieren Entitäten) werden als Text-files gespeichert (test/resources/informationextraction/output/evaluation_files). 
Bei der Evaluation des Bootstrapping-Ansatzes werden außerdem sämtliche automatisch generierten Patterns hinterlegt (test/resources/information_extraction/output).

______________________________________________________________________________________

Zur Ausführung der JUnit Testklassen müssen folgende Dateien hinzugefügt werden:

In den Ordner information_extraction/data/openNLPmodels: 
de-sent.bin & de-token.bin (downloadlink: http://opennlp.sourceforge.net/models-1.5/)

In den Ordner information_extraction/data/sentencedata_models: 
ger-tagger+lemmatizer+morphology+graph-based-3.6+.tgz (downloadlink: https://code.google.com/archive/p/mate-tools/downloads)

_______________________________________________________________________________________

1 Bundesinstitut für Berufsbildung
