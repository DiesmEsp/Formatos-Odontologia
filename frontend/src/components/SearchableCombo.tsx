import { useState, useRef, useEffect, useLayoutEffect } from 'react';
import { createPortal } from 'react-dom';
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

interface DropdownCoords {
  top: number;
  left: number;
  width: number;
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
  const [coords, setCoords] = useState<DropdownCoords | null>(null);
  const containerRef = useRef<HTMLDivElement>(null);
  const boxRef = useRef<HTMLDivElement>(null);
  const dropdownRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  const selectedOption = options.find((o) => o.id === value);

  const filteredOptions = query.trim()
    ? options.filter((o) => o.label.toLowerCase().includes(query.toLowerCase()))
    : options;

  const updatePosition = () => {
    if (boxRef.current) {
      const rect = boxRef.current.getBoundingClientRect();
      setCoords({ top: rect.bottom, left: rect.left, width: rect.width });
    }
  };

  useLayoutEffect(() => {
    if (open) updatePosition();
  }, [open]);

  useEffect(() => {
    if (!open) return;
    const reposition = () => updatePosition();
    window.addEventListener('resize', reposition);
    window.addEventListener('scroll', reposition, true);
    return () => {
      window.removeEventListener('resize', reposition);
      window.removeEventListener('scroll', reposition, true);
    };
  }, [open]);

  useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      const target = e.target as Node;
      if (containerRef.current?.contains(target)) return;
      if (dropdownRef.current?.contains(target)) return;
      setOpen(false);
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  useEffect(() => {
    const t = setTimeout(() => onSearch?.(query), 250);
    return () => clearTimeout(t);
  }, [query, onSearch]);

  const handleSelect = (id: number) => {
    onChange(id);
    setOpen(false);
    setQuery('');
  };

  const dropdown = open && coords ? (
    <div
      className="combo-dropdown"
      ref={dropdownRef}
      style={{ position: 'fixed', top: coords.top, left: coords.left, width: coords.width, right: 'auto' }}
    >
      {loading && <div className="combo-dropdown-loading">Buscando...</div>}
      {!loading && filteredOptions.length === 0 && !allowCreate && (
        <div className="combo-dropdown-empty">Sin resultados</div>
      )}
      {!loading && filteredOptions.map((opt) => (
        <button
          type="button"
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
      {!loading && filteredOptions.length === 0 && allowCreate && onCreateNew && query.trim() && (
        <div style={{ padding: '12px', textAlign: 'center' }}>
          <span className="text-muted text-sm" style={{ display: 'block', marginBottom: 8 }}>Sin resultados</span>
          <button type="button" className="btn btn-ghost btn-sm" onClick={() => { setOpen(false); setQuery(''); onCreateNew(); }}>
            <Plus size={14} /> Crear &quot;{query}&quot;
          </button>
        </div>
      )}
      {!loading && allowCreate && onCreateNew && filteredOptions.length > 0 && (
        <button type="button" className="combo-option combo-option-create" onClick={() => { setOpen(false); setQuery(''); onCreateNew(); }}>
          <Plus size={14} />
          <span>Crear nuevo</span>
        </button>
      )}
    </div>
  ) : null;

  return (
    <div className={`searchable-combo ${className}`} ref={containerRef}>
      <div className="search-box" ref={boxRef} onClick={() => !disabled && setOpen(true)}>
        <Search size={14} className="search-box-icon" />
        {open ? (
          <input
            ref={inputRef}
            type="text"
            className="search-box-input"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder={placeholder}
            autoFocus
          />
        ) : (
          <span className={`search-box-value ${!selectedOption ? 'placeholder' : ''}`}>
            {selectedOption ? selectedOption.label : placeholder}
          </span>
        )}
      </div>

      {dropdown && createPortal(dropdown, document.body)}
    </div>
  );
}
