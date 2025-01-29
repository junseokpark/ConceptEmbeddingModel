"Umls" disk-based database that can be used by the new ctakes-dictionary-lookup2 module.  This is fairly performant, but not nearly as fast as the in-memory version.

The table is named ctakes_umls and contains the fields cui, tui, rindex, tcount, text, rword.

All Snomed Cuis of the cTakes semantic groups are represented by Snomed term text and synonyms from other umls sources.

While the .data file is not plain text, any sql client can open and explore it.

hsql requires that the database url be all lower-case, so place the dictionary in files and directories with lower-case names.