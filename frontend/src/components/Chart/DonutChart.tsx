import { PieChart as RechartsPie, Pie, Cell, Tooltip, ResponsiveContainer, Legend } from 'recharts';
import { COLORS_CHART } from '../../lib/constants';

interface DonutChartProps {
  data: { label: string; valor: number }[];
  title?: string;
}

export function DonutChart({ data, title }: DonutChartProps) {
  return (
    <div className="chart-card">
      {title && (
        <div className="chart-head">
          <span className="chart-title">{title}</span>
        </div>
      )}
      <ResponsiveContainer width="100%" height={220}>
        <RechartsPie>
          <Pie
            data={data}
            dataKey="valor"
            nameKey="label"
            cx="50%"
            cy="50%"
            innerRadius={55}
            outerRadius={85}
            paddingAngle={2}
          >
            {data.map((_, idx) => (
              <Cell key={idx} fill={COLORS_CHART[idx % COLORS_CHART.length]} />
            ))}
          </Pie>
          <Tooltip
            contentStyle={{
              borderRadius: '6px',
              border: '1px solid #dce4e4',
              boxShadow: '0 4px 6px -1px rgb(20 42 51 / 0.06)',
              fontSize: '12px',
            }}
          />
          <Legend
            wrapperStyle={{ fontSize: '11px', color: '#5c7178' }}
            iconType="circle"
            iconSize={8}
          />
        </RechartsPie>
      </ResponsiveContainer>
    </div>
  );
}
