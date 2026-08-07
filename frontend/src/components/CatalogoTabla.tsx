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
}: CatalogoTablaProps<T>) {
  const [query, setQuery] = useState('');

  useEffect(() => {
    if (!searchEnabled) return;
    const t = setTimeout(() => onSearch?.(query), 300);
    return () => clearTimeout(t);
  }, [query, searchEnabled, onSearch]);

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
            {data.map((row, idx) => (
              <tr key={(row as any).id ?? idx}>
                {columns.map((col) => (
                  <td key={col.key} className={col.className}>
                    {col.render(row)}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
        </div>
      )}
    </div>
  );
}
