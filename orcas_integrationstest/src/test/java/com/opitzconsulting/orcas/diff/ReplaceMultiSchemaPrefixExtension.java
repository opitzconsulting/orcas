package com.opitzconsulting.orcas.diff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.opitzconsulting.orcas.diff.OrcasCoreIntegrationTest.MultiSchemaSetup;
import de.opitzconsulting.orcasDsl.ForeignKey;
import de.opitzconsulting.orcasDsl.Index;
import de.opitzconsulting.orcasDsl.IndexExTable;
import de.opitzconsulting.orcasDsl.IndexOrUniqueKey;
import de.opitzconsulting.orcasDsl.Model;
import de.opitzconsulting.orcasDsl.ModelElement;
import de.opitzconsulting.orcasDsl.Mview;
import de.opitzconsulting.orcasDsl.Sequence;
import de.opitzconsulting.orcasDsl.Table;
import de.opitzconsulting.orcasDsl.UniqueKey;

public class ReplaceMultiSchemaPrefixExtension {
    private Map<String, String> replaceMap = new HashMap<>();

    public Model transformModel(Model pModel) {
        for (ModelElement lModelElement : new ArrayList<>(pModel.getModel_elements())) {
            if (lModelElement instanceof Table) {
                Table lTable = (Table) lModelElement;

                int i = lTable.getName().indexOf('.');
                if (i >= 0) {
                    String lOwner = lTable.getName().substring(0, i + 1);
                    lTable.setName(handleOwner(lTable.getName(), lOwner));

                    if (lTable.getPrimary_key() != null && lTable.getPrimary_key().getIndexname() != null) {
                        String lIndexOwner = lOwner;
                        int j = lTable.getPrimary_key().getIndexname().indexOf('.');
                        if (j >= 0) {
                            lIndexOwner = lTable.getPrimary_key().getIndexname().substring(0, j + 1);
                        }
                        lTable.getPrimary_key().setIndexname(handleOwner(lTable.getPrimary_key().getIndexname(), lIndexOwner));
                    }

                    for (ForeignKey lForeign_key : lTable.getForeign_keys()) {
                        String lDestOwner = lOwner;
                        int j = lForeign_key.getDestTable().indexOf('.');
                        if (j >= 0) {
                            lDestOwner = lForeign_key.getDestTable().substring(0, j + 1);
                        }
                        lForeign_key.setDestTable(handleOwner(lForeign_key.getDestTable(), lDestOwner));
                    }

                    for (IndexOrUniqueKey lIndex : lTable.getInd_uks()) {
                        if (lIndex instanceof Index) {
                            String lIndexOwner = lOwner;
                            int j = lIndex.getConsName().indexOf('.');
                            if (j >= 0) {
                                lIndexOwner = lIndex.getConsName().substring(0, j + 1);
                            }
                            lIndex.setConsName(handleOwner(lIndex.getConsName(), lIndexOwner));
                        }

                        if (lIndex instanceof UniqueKey) {
                            UniqueKey lUniqueKey = (UniqueKey) lIndex;
                            if (lUniqueKey.getIndexname() != null) {
                                String lIndexOwner = lOwner;
                                int j = lUniqueKey.getIndexname().indexOf('.');
                                if (j >= 0) {
                                    lIndexOwner = lUniqueKey.getIndexname().substring(0, j + 1);
                                }
                                lUniqueKey.setIndexname(handleOwner(lUniqueKey.getIndexname(), lIndexOwner));
                            }
                        }
                    }
                }
            }

            if (lModelElement instanceof Sequence) {
                Sequence lSequence = (Sequence) lModelElement;
                int i = lSequence.getSequence_name().indexOf('.');
                if (i >= 0) {
                    String lOwner = lSequence.getSequence_name().substring(0, i + 1);
                    lSequence.setSequence_name(handleOwner(lSequence.getSequence_name(), lOwner));
                }
            }

            if (lModelElement instanceof IndexExTable) {
                IndexExTable lIndexExTable = (IndexExTable) lModelElement;
                int i = lIndexExTable.getIndex_name().indexOf('.');
                if (i >= 0) {
                    String lOwner = lIndexExTable.getIndex_name().substring(0, i + 1);
                    lIndexExTable.setIndex_name(handleOwner(lIndexExTable.getIndex_name(), lOwner));
                }
            }

            if (lModelElement instanceof Mview) {
                Mview lMview = (Mview) lModelElement;
                int i = lMview.getMview_name().indexOf('.');
                if (i >= 0) {
                    String lOwner = lMview.getMview_name().substring(0, i + 1);
                    lMview.setMview_name(handleOwner(lMview.getMview_name(), lOwner));
                }
            }

        }

        return pModel;
    }

    private String handleOwner(String pStringWithOwner, String pOwner) {
        if (replaceMap.containsKey(pOwner)) {
            return replaceMap.get(pOwner) + pStringWithOwner.replace(pOwner, "");
        }
        return pStringWithOwner;
    }

    public void initSchema(MultiSchemaSetup pMultiSchemaSetup) {
        replaceMap.put(pMultiSchemaSetup.getSchemaAlias() + ".", pMultiSchemaSetup.getSchemaName() + ".");
    }
}
