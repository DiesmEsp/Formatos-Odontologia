import type { LucideIcon } from 'lucide-react';

interface KpiCardProps {
  label: string;
  value: string | number;
  sub?: string;
  icon: LucideIcon;
  variant?: 'default' | 'success' | 'warning';
}

export function KpiCard({ label, value, sub, icon: Icon, variant = 'default' }: KpiCardProps) {
  const iconClass = variant === 'success' ? 'kpi-icon-ok' : variant === 'warning' ? 'kpi-icon-warn' : '';
  return (
    <div className="kpi-card">
      <div className="kpi-card-row">
        <div className="kpi-card-info">
          <span className="kpi-label">{label}</span>
          <span className="kpi-value">{value}</span>
          {sub && <span className="kpi-sub">{sub}</span>}
        </div>
        <div className={`kpi-icon ${iconClass}`}>
          <Icon size={18} />
        </div>
      </div>
    </div>
  );
}
