import { LineChart as RechartsLine, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { COLORS_CHART } from '../../lib/constants';

interface LineChartProps {
  data: { label: string; valor: number }[];
  title?: string;
  color?: string;
}

export function LineChart({ data, title, color = COLORS_CHART[0] }: LineChartProps) {
  return (
    <div className="chart-card">
      {title && (
        <div className="chart-head">
          <span className="chart-title">{title}</span>
        </div>
      )}
      <ResponsiveContainer width="100%" height={220}>
        <RechartsLine data={data}>
          <CartesianGrid strokeDasharray="3 3" stroke="var(--color-border)" />
          <XAxis dataKey="label" tick={{ fontSize: 11, fill: 'var(--color-text-secondary)' }} axisLine={{ stroke: 'var(--color-border)' }} />
          <YAxis tick={{ fontSize: 11, fill: 'var(--color-text-secondary)' }} axisLine={{ stroke: 'var(--color-border)' }} width={60} />
          <Tooltip
            contentStyle={{
              borderRadius: '6px',
              border: '1px solid var(--color-border)',
              boxShadow: '0 4px 6px -1px rgb(20 42 51 / 0.06)',
              fontSize: '12px',
            }}
            formatter={(value: number) => [`S/ ${value.toFixed(2)}`, 'Monto']}
          />
          <Line type="monotone" dataKey="valor" stroke={color} strokeWidth={2} dot={{ fill: color, r: 4 }} activeDot={{ r: 6 }} />
        </RechartsLine>
      </ResponsiveContainer>
    </div>
  );
}
