import { BarChart as RechartsBar, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { COLORS_CHART } from '../../lib/constants';

interface BarChartProps {
  data: { label: string; valor: number }[];
  title?: string;
  color?: string;
}

export function BarChart({ data, title, color = COLORS_CHART[0] }: BarChartProps) {
  return (
    <div className="chart-card">
      {title && (
        <div className="chart-head">
          <span className="chart-title">{title}</span>
        </div>
      )}
      <ResponsiveContainer width="100%" height={220}>
        <RechartsBar data={data}>
          <CartesianGrid strokeDasharray="3 3" stroke="var(--color-border)" />
          <XAxis dataKey="label" tick={{ fontSize: 11, fill: 'var(--color-text-secondary)' }} axisLine={{ stroke: 'var(--color-border)' }} />
          <YAxis tick={{ fontSize: 11, fill: 'var(--color-text-secondary)' }} axisLine={{ stroke: 'var(--color-border)' }} width={50} />
          <Tooltip
            contentStyle={{
              borderRadius: '6px',
              border: '1px solid var(--color-border)',
              boxShadow: '0 4px 6px -1px rgb(20 42 51 / 0.06)',
              fontSize: '12px',
            }}
          />
          <Bar dataKey="valor" fill={color} radius={[4, 4, 0, 0]} />
        </RechartsBar>
      </ResponsiveContainer>
    </div>
  );
}
