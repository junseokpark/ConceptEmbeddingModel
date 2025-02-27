-- Definitions : https://www.ncbi.nlm.nih.gov/books/NBK9685/
-- Concepts, Concept Names, and their sources (2.3) = MRCONSO.RRF
-- Attributes (2.5) = MRSAT.RRF, MRDEF.RRF, MRSTY.RRF, MRHIST.RRF
-- Relationships (2.4) = MRREL.RRF, MRCXT.RRF, MRHIER.RRF, MRMAP.RRF, MRSMAP.RRF
-- Data about the Metathesaurus (2.6) = MRFILES.RRF, MRCOLS.RRF, MRDOC.RRF, MRRANK.RRF, MRSAB.RRF, AMBIGLUI.RRF, AMBIGSUI.RRF, CHANGE/MERGEDCUI.RRF, CHANGE/MERGEDLUI.RRF, CHANGE/DELETEDCUI.RRF, CHANGE/DELETEDLUI.RRF, CHANGE/DELETEDSUI.RRF, MRCUI.RRF
-- Indexes (2.7) = MRXW_BAQ.RRF, MRXW_DAN.RRF, MRXW_DUT.RRF, MRXW_ENG.RRF, MRXW_FIN.RRF, MRXW_FRE.RRF, MRXW_GER.RRF, MRXW_HEB.RRF, MRXW_HUN.RRF, MRXW_ITA.RRF, MRXW_NOR.RRF, MRXW_POR.RRF, MRXW_RUS.RRF, MRXW_SPA.RRF, MRXW_SWE.RRF, MRXNW_ENG.RRF, MRXNS_ENG.RRF

SELECT
  DISTINCT
  SAB
FROM MRDEF a;
-- 31 Sources

SELECT
  COUNT(*) FROM MRDEF;
-- 209,029

SELECT
  COUNT(*) FROM MRCONSO;
-- 6,091,125

SELECT COUNT(*) FROM (
                       SELECT
                         DISTINCT CUI FROM MRCONSO) a;
-- 2,381,701 CUIS

SELECT
  A.CUI,
  B.STR,
  A.DEF
  FROM MRDEF A, (SELECT DISTINCT CUI, STR FROM MRCONSO) B
WHERE A.CUI = B.CUI;

SELECT
  CUI
FROM MRDEF
ORDER BY CUI;


SELECT
  DISTINCT
  SAB
FROM MRDEF a;
-- 31 Sources

SELECT
  COUNT(*) FROM MRDEF;
-- 209,029

SELECT
  COUNT(*) FROM MRCONSO;
-- 6,091,125

SELECT COUNT(*) FROM (
                       SELECT
                         DISTINCT CUI FROM MRCONSO) a;
-- 2,381,701



CREATE table MRDEF_WIKI like CUI_WikiDef;
INSERT MRDEF_WIKI SELECT * FROM CUI_WikiDef;


SELECT
  CUI,
  GROUP_CONCAT(DEF, SEPARATOR ' ') DEF
FROM (
       SELECT a.CUI,CONCAT('[',a.STR,'] ',a.DEF) DEF FROM CUI_WikiDef a
       UNION
       SELECT a.CUI,CONCAT('[',b.STR,']',a.DEF) DEF FROM MRDEF a, (SELECT CUI, GROUP_CONCAT(STR SEPARATOR ' , ') STR FROM MRCONSO GROUP BY CUI) b WHERE a.CUI = b.CUI) A;


