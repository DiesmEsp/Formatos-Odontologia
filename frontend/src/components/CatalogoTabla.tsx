import { useState, useEffect } from 'react';
import type { ReactNode } from 'react';
import { Search } from 'lucide-react';

export interface Column<T> {
  key: string;
  header: string;
  width?: number;
  render: (row: T) => ReactNode;
  className?: string;
}

interface CatalogoTablaProps<T> {
  columns: Column<T>[];
  data: T[];
  loading: boolean;
  searchPlaceholder?: string;
  searchEnabled?: boolean;
  onSearch?: (query: string) => void;
  emptyTitle?: string;
  emptyText?: string;
  totalLabel?: string;
  filterBar?: ReactNode;
  rowKey?: (row: T) => string | number;
  renderDetail?: (row: T) => ReactNode;
  expanded?: Set<string | number>;
}

export function CatalogoTabla<T>({
  columns,
  data,
  loading,
  searchPlaceholder = 'Buscar...',
  searchEnabled = true,
  onSearch,
  emptyTitle = 'Sin resultados',
  emptyText = 'No se encontraron registros.',
  totalLabel,
  filterBar,
  rowKey,
  renderDetail,
  expanded,
}: CatalogoTablaProps<T>) {
  const [query, setQuery] = useState('');

  useEffect(() => {
    if (!searchEnabled) return;
    const t = setTimeout(() => onSearch?.(query), 300);
    return () => clearTimeout(t);
  }, [query, searchEnabled, onSearch]);

  const keyOf = (row: T, idx: number): string | number => {
    if (rowKey) return rowKey(row);
    return (row as any).id ?? idx;
  };

  return (
    <div>
      <div className="filter-bar">
        {searchEnabled && (
          <div className="search-box" style={{ maxWidth: 300 }}>
            <Search size={14} className="search-box-icon" />
            <input
              type="text"
              className="search-box-input"
              placeholder={searchPlaceholder}
              value={query}
              onChange={(e) => setQuery(e.target.value)}
            />
          </div>
        )}
        {filterBar}
        <span className="text-muted text-sm">
          {totalLabel ?? `${data.length} registros`}
        </span>
      </div>

      {loading ? (
        <div className="empty-state"><span className="empty-text">Cargando...</span></div>
      ) : data.length === 0 ? (
        <div className="empty-state">
          <span className="empty-title">{emptyTitle}</span>
          <span className="empty-text">{emptyText}</span>
        </div>
      ) : (
        <div className="table-container">
        <table className="data-table">
          <thead>
            <tr>
              {columns.map((col) => (
                <th key={col.key} style={col.width ? { width: col.width } : undefined}>
                  {col.header}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {data.map((row, idx) => {
              const key = keyOf(row, idx);
              return (
                <CatalogoTablaRow
                  key={key}
                  row={row}
                  columns={columns}
                  expanded={!!(renderDetail && expanded?.has(key))}
                  detail={renderDetail ? renderDetail(row) : null}
                />
              );
            })}
          </tbody>
        </table>
        </div>
      )}
    </div>
  );
}

function CatalogoTablaRow<T>({
  row,
  columns,
  expanded,
  detail,
}: {
  row: T;
  columns: Column<T>[];
  expanded: boolean;
  detail: ReactNode | null;
}) {
  return (
    <>
      <tr>
        {columns.map((col) => (
          <td key={col.key} className={col.className}>
            {col.render(row)}
          </td>
        ))}
      </tr>
      {expanded && detail != null && (
        <tr className="row-detail-row">
          <td colSpan={columns.length}>
            <div className="row-detail">{detail}</div>
          </td>
        </tr>
      )}
    </>
  );
}
