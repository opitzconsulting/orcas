package de.opitzconsulting.orcas.diff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EStructuralFeature;

import de.opitzconsulting.orcas.diff.DiffReasonKey.DiffReasonKeyRegistry;
import de.opitzconsulting.orcas.orig.diff.AbstractDiff;
import de.opitzconsulting.orcas.orig.diff.ColumnRefDiff;
import sun.util.resources.th.CalendarData_th;

class RecreateNeededBuilder<T extends AbstractDiff> implements RecreateNeededBuilderHandler<T> {
    interface RecreateNeededBuilderRunnable<T extends AbstractDiff> extends Consumer<RecreateNeededBuilderHandler<T>> {
    }

    private T diff;
    private List<EStructuralFeature> eAttributes = new ArrayList<>();
    private List<RecreateNeededBuilderRunnable<T>> additionalHandlers = new ArrayList<>();
    private DiffReasonKeyRegistry diffReasonKeyRegistry;
    private Map<AbstractDiff, List<DiffActionReason>> recreateDiffDiffActionReasonMap = new HashMap<>();

    @Override
    public T getDiff() {
        return diff;
    }

    @Override
    public void setRecreateNeededDifferent(List<Difference> pDiffReasonDetails) {
        DiffActionReasonDifferent lDiffActionReasonDifferent = new DiffActionReasonDifferent(diffReasonKeyRegistry.getDiffReasonKey(diff));

        for (Difference lDifference : pDiffReasonDetails) {
            lDiffActionReasonDifferent.addDiffReasonDetail(lDifference);
        }

        addRecreateNeeded(lDiffActionReasonDifferent);
    }

    private void addRecreateNeeded(DiffActionReason pDiffActionReason) {
        List<DiffActionReason> lDiffActionReasonList = recreateDiffDiffActionReasonMap.get(diff);

        if (lDiffActionReasonList == null) {
            lDiffActionReasonList = new ArrayList<>();
            recreateDiffDiffActionReasonMap.put(diff, lDiffActionReasonList);
        }

        lDiffActionReasonList.add(pDiffActionReason);
    }

    @Override
    public void setRecreateNeededDependsOn(List<DiffActionReason> pDiffActionReasonDependsOnList) {
        addRecreateNeeded(new DiffActionReasonDependsOn(diffReasonKeyRegistry.getDiffReasonKey(diff), pDiffActionReasonDependsOnList));
    }

    @Override
    public void setRecreateNeededDifferentAttributes(List<EStructuralFeature> pDiffReasonDetails) {
        setRecreateNeededDifferent(pDiffReasonDetails.stream().map(p -> new DifferenceImplAttributeOnly(p)).collect(Collectors.toList()));
    }

    public RecreateNeededBuilder(
        T pDiff,
        DiffReasonKeyRegistry pDiffReasonKeyRegistry,
        Map<AbstractDiff, List<DiffActionReason>> pRecreateDiffDiffActionReasonMap) {
        diff = pDiff;
        diffReasonKeyRegistry = pDiffReasonKeyRegistry;
        recreateDiffDiffActionReasonMap = pRecreateDiffDiffActionReasonMap;
    }

    public RecreateNeededBuilder<T> ifX(RecreateNeededBuilderRunnable<T> pObject) {
        additionalHandlers.add(pObject);

        return this;
    }

    public RecreateNeededBuilder<T> ifColumnDependentRecreate(
        Map<String, List<DiffActionReason>> pRecreateColumnNames,
        List<ColumnRefDiff> pColumnRefDiff) {
        return ifX(p ->
        {
            handleColumnDependentRecreate(pRecreateColumnNames, pColumnRefDiff);
        });
    }

    private void handleColumnDependentRecreate(Map<String, List<DiffActionReason>> pRecreateColumnNames, List<ColumnRefDiff> pColumnDiffList) {
        List<DiffActionReason> lDependsOnList = new ArrayList<>();

        for (ColumnRefDiff lColumnRefDiff : pColumnDiffList) {
            if (lColumnRefDiff.isOld) {
                if (pRecreateColumnNames.keySet().contains(lColumnRefDiff.column_nameOld)) {
                    lDependsOnList.addAll(pRecreateColumnNames.get(lColumnRefDiff.column_nameOld));
                }
            }
        }

        if (!lDependsOnList.isEmpty()) {
            setRecreateNeededDependsOn(lDependsOnList);
        }
    }

    public RecreateNeededBuilder<T> ifColumnDependentRecreate(Map<String, List<DiffActionReason>> pRecreateColumnNames, String pColumnName) {
        return ifX(p ->
        {
            handleColumnDependentRecreate(pRecreateColumnNames, pColumnName);
        });
    }

    private void handleColumnDependentRecreate(Map<String, List<DiffActionReason>> pRecreateColumnNames, String pColumnName) {
        List<DiffActionReason> lDependsOnList = new ArrayList<>();

        if (pRecreateColumnNames.keySet().contains(pColumnName)) {
            lDependsOnList.addAll(pRecreateColumnNames.get(pColumnName));
        }

        if (!lDependsOnList.isEmpty()) {
            setRecreateNeededDependsOn(lDependsOnList);
        }
    }

    public RecreateNeededBuilder<T> ifDifferent(EStructuralFeature pEAttribute, boolean pCheckThis) {
        if (pCheckThis) {
            eAttributes.add(pEAttribute);
        }

        return this;
    }

    public RecreateNeededBuilder<T> ifDifferent(EStructuralFeature pEAttribute) {
        eAttributes.add(pEAttribute);

        return this;
    }

    public RecreateNeededBuilder<T> ifDifferentName(
        EStructuralFeature pEAttribute,
        List<String> pOldNames,
        String pNewName,
        String pOldName,
        boolean pIsRenamePossible) {
        if (pNewName == null || pOldName == null || pOldNames.contains(pNewName) || !pIsRenamePossible) {
            ifDifferent(pEAttribute);
        }

        return this;
    }

    public RecreateNeededBuilder<T> ifDifferentName(
        EStructuralFeature pEAttribute,
        Map<String, List<String>> pOldNames,
        String pNewName,
        String pOldName,
        boolean pIsRenamePossible) {
        if (pNewName == null || pOldName == null || !pIsRenamePossible) {
            ifDifferent(pEAttribute);
        }

        if (pOldNames.values().stream().filter(p -> p.contains(pNewName)).findAny().isPresent()) {
            ifDifferent(pEAttribute);
        }

        return this;
    }

    public void calculate() {
        if (diff.isMatched) {
            List<Difference> lDifferentEAttributes = getDifferentEAttributes(diff, eAttributes);
            if (!lDifferentEAttributes.isEmpty()) {
                setRecreateNeededDifferent(lDifferentEAttributes);
            }

            for (RecreateNeededBuilderRunnable<T> lRunnable : additionalHandlers) {
                lRunnable.accept(this);
            }
        }
    }

    public interface Difference {
        EStructuralFeature getEAttribute();

        Object getOldValue();

        Object getNewValue();
    }

    public static class DifferenceImpl implements Difference {
        private EStructuralFeature eattribute;
        private AbstractDiff abstractDiff;

        public DifferenceImpl(EStructuralFeature pEattribute, AbstractDiff pAbstractDiff) {
            eattribute = pEattribute;
            abstractDiff = pAbstractDiff;
        }

        @Override
        public EStructuralFeature getEAttribute() {
            return eattribute;
        }

        @Override
        public Object getOldValue() {
            try {
                return abstractDiff.getValue(eattribute, false);
            } catch (Exception e) {
                try {
                    return ((List<ColumnRefDiff>)abstractDiff.getDiff(eattribute)).stream().filter(p->p.isOld).map(p->p.column_nameOld).collect(Collectors.joining(","));
                } catch (Exception e1) {
                    return "object";
                }
            }
        }

        @Override
        public Object getNewValue() {
            try {
                return abstractDiff.getValue(eattribute, true);
            } catch (Exception e) {
                try {
                    return ((List<ColumnRefDiff>)abstractDiff.getDiff(eattribute)).stream().filter(p->p.isNew).map(p->p.column_nameNew).collect(Collectors.joining(","));
                } catch (Exception e1) {
                    return "object";
                }
            }
        }
    }

    public static class DifferenceImplAttributeOnly implements Difference {
        private EStructuralFeature eattribute;

        public DifferenceImplAttributeOnly(EStructuralFeature pEattribute) {
            eattribute = pEattribute;
        }

        @Override
        public EStructuralFeature getEAttribute() {
            return eattribute;
        }

        @Override
        public Object getOldValue() {
            return null;
        }

        @Override
        public Object getNewValue() {
            return null;
        }
    }

    static List<Difference> getDifferentEAttributes(AbstractDiff pDiff, List<EStructuralFeature> pEAttributes) {
        List<Difference> lReturn = new ArrayList<>();

        for (EStructuralFeature lEAttribute : pEAttributes) {
            if (!pDiff.isFieldEqual(lEAttribute)) {
                lReturn.add(new DifferenceImpl(lEAttribute, pDiff));
            }
        }

        return lReturn;
    }
}
