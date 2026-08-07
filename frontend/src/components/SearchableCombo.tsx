import { useState, useRef, useEffect, useCallback } from 'react';
import { Search, Plus } from 'lucide-react';
import { Badge } from './Badge';

export interface SearchableOption {
  id: number;
  label: string;
  extra?: string;
  badge?: string;
}

interface SearchableComboProps {
  options: SearchableOption[];
  value: number | null;
  onChange: (id: number | null) => void;
  placeholder?: string;
  disabled?: boolean;
  allowCreate?: boolean;
  onCreateNew?: () => void;
  onSearch?: (query: string) => void;
  loading?: boolean;
  className?: string;
}

export function SearchableCombo({
  options,
  value,
  onChange,
  placeholder = 'Seleccionar...',
  disabled = false,
  allowCreate = false,
  onCreateNew,
  onSearch,
  loading = false,
  className = '',
}: SearchableComboProps) {
  const [open, setOpen] = useState(false);
  const [query, setQuery] = useState('');
  const containerRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  const selectedOption = options.find((o) => o.id === value);

  useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      if (containerRef.current && !containerRef.current.contains(e.target as Node)) {
        setOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleSearch = useCallback((q: string) => {
    setQuery(q);
    onSearch?.(q);
  }, [onSearch]);

  const handleSelect = (id: number) => {
    onChange(id);
    setOpen(false);
    setQuery('');
  };

  return (
    <div className={`searchable-combo ${className}`} ref={containerRef}>
      <div className="search-box" onClick={() => !disabled && setOpen(true)}>
        <Search size={14} className="search-box-icon" />
        {open ? (
          <input
            ref={inputRef}
            type="text"
            className="search-box-input"
            value={query}
            onChange={(e) => handleSearch(e.target.value)}
            placeholder={placeholder}
            autoFocus
          />
        ) : (
          <span className={`search-box-value ${!selectedOption ? 'placeholder' : ''}`}>
            {selectedOption ? selectedOption.label : placeholder}
          </span>
        )}
      </div>

      {open && (
        <div className="combo-dropdown">
          {loading && <div className="combo-dropdown-loading">Buscando...</div>}
          {!loading && options.length === 0 && !allowCreate && (
            <div className="combo-dropdown-empty">Sin resultados</div>
          )}
          {!loading && options.map((opt) => (
            <button
              key={opt.id}
              className={`combo-option ${opt.id === value ? 'selected' : ''}`}
              onClick={() => handleSelect(opt.id)}
            >
              <span className="combo-option-id">{String(opt.id).padStart(3, '0')}</span>
              <span className="combo-option-name">{opt.label}</span>
              {opt.badge && <Badge variant="info">{opt.badge}</Badge>}
              {opt.extra && <span className="combo-option-extra">{opt.extra}</span>}
            </button>
          ))}
          {!loading && options.length === 0 && allowCreate && onCreateNew && query.trim() && (
            <div style={{ padding: '12px', textAlign: 'center' }}>
              <span className="text-muted text-sm" style={{ display: 'block', marginBottom: 8 }}>Sin resultados</span>
              <button className="btn btn-ghost btn-sm" onClick={() => { setOpen(false); setQuery(''); onCreateNew(); }}>
                <Plus size={14} /> Crear &quot;{query}&quot;
              </button>
            </div>
          )}
          {!loading && allowCreate && onCreateNew && options.length > 0 && (
            <button className="combo-option combo-option-create" onClick={() => { setOpen(false); setQuery(''); onCreateNew(); }}>
              <Plus size={14} />
              <span>Crear nuevo</span>
            </button>
          )}
        </div>
      )}
    </div>
  );
}
