interface ToothMarkProps {
  size?: number;
  color?: string;
}

export function ToothMark({ size = 20, color = 'currentColor' }: ToothMarkProps) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke={color}
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <path d="M12 5.5c-2.6 0-4.6 1.4-5.5 3.4-.7 1.5-.6 3.4.2 5.5.4 1 .6 2 .6 3.1 0 1 .5 1.8 1.5 1.8.7 0 1.2-.5 1.2-1.2 0-1-.2-2-.3-3 .4-.3 1.3-.6 2.3-.6s1.9.3 2.3.6c-.1 1-.3 2-.3 3 0 .7.5 1.2 1.2 1.2 1 0 1.5-.8 1.5-1.8 0-1.1.2-2.1.6-3.1.8-2.1.9-4 .2-5.5-.9-2-2.9-3.4-5.5-3.4z" />
    </svg>
  );
}
