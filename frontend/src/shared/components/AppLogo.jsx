const VARIANTES = {
  light: {
    main: '#FFFFFF',
    accent: '#8EAFD6',
  },
  dark: {
    main: '#19426B',
    accent: '#5B8FC9',
  },
};

export default function AppLogo({
  className = 'h-10 w-10',
  variant = 'dark',
  alt = 'Logo PracTI',
}) {
  const { main, accent } = VARIANTES[variant] ?? VARIANTES.dark;

  return (
    <svg
      viewBox="0 0 100 100"
      className={className}
      role="img"
      aria-label={alt}
      xmlns="http://www.w3.org/2000/svg"
    >
      <title>{alt}</title>
      <line
        x1="28"
        y1="30"
        x2="72"
        y2="30"
        stroke={main}
        strokeWidth="7"
        strokeLinecap="round"
      />
      <line
        x1="72"
        y1="30"
        x2="50"
        y2="72"
        stroke={main}
        strokeWidth="7"
        strokeLinecap="round"
      />
      <line
        x1="28"
        y1="30"
        x2="50"
        y2="72"
        stroke={accent}
        strokeWidth="7"
        strokeLinecap="round"
      />
      <circle cx="28" cy="30" r="11" fill={main} />
      <circle cx="72" cy="30" r="11" fill={main} />
      <circle cx="50" cy="72" r="11" fill={main} />
    </svg>
  );
}
