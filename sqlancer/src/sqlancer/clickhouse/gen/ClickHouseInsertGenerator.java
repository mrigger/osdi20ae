package sqlancer.clickhouse.gen;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import sqlancer.clickhouse.ClickHouseErrors;
import sqlancer.clickhouse.ClickHouseProvider.ClickHouseGlobalState;
import sqlancer.clickhouse.ClickHouseSchema.ClickHouseColumn;
import sqlancer.clickhouse.ClickHouseSchema.ClickHouseTable;
import sqlancer.clickhouse.ClickHouseToStringVisitor;
import sqlancer.common.gen.AbstractInsertGenerator;
import sqlancer.common.query.ExpectedErrors;
import sqlancer.common.query.Query;
import sqlancer.common.query.QueryAdapter;

public class ClickHouseInsertGenerator extends AbstractInsertGenerator<ClickHouseColumn> {

    private final ClickHouseGlobalState globalState;
    private final ExpectedErrors errors = new ExpectedErrors();
    private final ClickHouseExpressionGenerator gen;

    public ClickHouseInsertGenerator(ClickHouseGlobalState globalState) {
        this.globalState = globalState;
        gen = new ClickHouseExpressionGenerator(globalState);
        errors.add("Cannot insert NULL value into a column of type");
        errors.add("Memory limit");
        errors.add("Cannot parse string");
        errors.add("Cannot parse Int32 from String, because value is too short");
        errors.add("does not return a value of type UInt8");
        ClickHouseErrors.addExpectedExpressionErrors(errors);
    }

    public static Query getQuery(ClickHouseGlobalState globalState) throws SQLException {
        return new ClickHouseInsertGenerator(globalState).get();
    }

    private Query get() {
        ClickHouseTable table = globalState.getSchema().getRandomTable(t -> !t.isView());
        List<ClickHouseColumn> columns = table.getRandomNonEmptyColumnSubset().stream()
                .filter(c -> !c.isAlias() && !c.isMaterialized()).collect(Collectors.toList());
        sb.append("INSERT INTO ");
        sb.append(table.getName());
        sb.append("(");
        sb.append(columns.stream().map(c -> c.getName()).collect(Collectors.joining(", ")));
        sb.append(")");
        sb.append(" VALUES ");
        insertColumns(columns);
        return new QueryAdapter(sb.toString(), errors);
    }

    @Override
    protected void insertValue(ClickHouseColumn column) {
        String s = ClickHouseToStringVisitor.asString(gen.generateConstant(column.getType()));
        sb.append(s);
    }

}
