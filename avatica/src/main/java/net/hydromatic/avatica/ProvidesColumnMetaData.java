package net.hydromatic.avatica;

import java.util.List;

public interface ProvidesColumnMetaData {
  public List<ColumnMetaData> getColumnList();
}
