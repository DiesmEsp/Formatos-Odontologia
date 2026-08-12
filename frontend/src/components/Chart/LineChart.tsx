import { LineChart as RechartsLine, Line, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { COLORS_CHART } from '../../lib/constants';

interface LineChartProps {
  data: { label: string; valor: number }[];
  title?: string;
  color?: string;
}

export function LineChart({ data, title, color = COLORS_CHART[0] }: LineChartProps) {
  const gradientId = 'lineGradient';

  return (
    <div className="chart-card">
      {title && (
        <div className="chart-head">
          <span className="chart-title">{title}</span>
        </div>
      )}
      <ResponsiveContainer width="100%" height={220}>
        <RechartsLine data={data}>
          <defs>
            <linearGradient id={gradientId} x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor={color} stopOpacity={0.2} />
              <stop offset="100%" stopColor={color} stopOpacity={0} />
            </linearGradient>
          </defs>
          <CartesianGrid strokeDasharray="3 3" stroke="var(--color-border)" vertical={false} />
          <XAxis dataKey="label" tick={{ fontSize: 11, fill: 'var(--color-text-secondary)' }} axisLine={{ stroke: 'var(--color-border)' }} tickLine={false} />
          <YAxis tick={{ fontSize: 11, fill: 'var(--color-text-secondary)' }} axisLine={false} tickLine={false} width={52} />
          <Tooltip
            contentStyle={{
              borderRadius: '6px',
              border: '1px solid var(--color-border)',
              boxShadow: '0 4px 6px -1px rgb(20 42 51 / 0.06)',
              fontSize: '12px',
            }}
            formatter={(value: number) => [`S/ ${value.toFixed(2)}`, 'Monto']}
          />
          <Area
            type="monotone"
            dataKey="valor"
            stroke="none"
            fill={`url(#${gradientId})`}
            animationDuration={1000}
            animationEasing="ease-in-out"
          />
          <Line
            type="monotone"
            dataKey="valor"
            stroke={color}
            strokeWidth={2.5}
            strokeLinecap="round"
            strokeLinejoin="round"
            dot={false}
            activeDot={{ r: 5, strokeWidth: 2, stroke: 'var(--color-surface)', fill: color }}
            animationDuration={1000}
            animationEasing="ease-in-out"
          />
        </RechartsLine>
      </ResponsiveContainer>
    </div>
  );
}
