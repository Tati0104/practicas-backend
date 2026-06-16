/** Paleta PracTI para gráficos e indicadores (modo claro) */
export const PALETA = {
  primary: '#19426B',
  primaryLight: '#E8EEF4',
  accent: '#D97706',
  emerald: '#059669',
  violet: '#7C3AED',
  red: '#DC2626',
  slate: '#64748B',
  grid: '#E2E8F0',
  text: '#334155',
  textMuted: '#94A3B8',
};

/** Paleta oscura — conserva marca PracTI con acentos más legibles sobre fondo carbón */
export const PALETA_OSCURA = {
  primary: '#2E6DA8',
  primaryLight: '#19426B',
  accent: '#F59E0B',
  emerald: '#10B981',
  violet: '#A78BFA',
  red: '#F87171',
  slate: '#94A3B8',
  grid: '#2A3544',
  text: '#E2E8F0',
  textMuted: '#64748B',
};

export function getPaleta(esOscuro = false) {
  return esOscuro ? PALETA_OSCURA : PALETA;
}

export function getTooltipStyle(esOscuro = false) {
  return {
    borderRadius: '10px',
    border: `1px solid ${esOscuro ? '#2A3544' : '#E2E8F0'}`,
    fontSize: '12px',
    backgroundColor: esOscuro ? '#1C2634' : '#ffffff',
    color: esOscuro ? '#E2E8F0' : '#334155',
    boxShadow: esOscuro ? '0 4px 24px rgba(0, 0, 0, 0.35)' : '0 4px 12px rgba(0, 0, 0, 0.08)',
  };
}

export function getSeries(esOscuro = false) {
  const p = getPaleta(esOscuro);
  return {
    primary: { fill: p.primary, stroke: p.primary },
    accent: { fill: p.accent, stroke: p.accent },
    emerald: { fill: p.emerald, stroke: p.emerald },
    violet: { fill: p.violet, stroke: p.violet },
    red: { fill: p.red, stroke: p.red },
  };
}

/** @deprecated use getSeries(esOscuro) */
export const SERIES = getSeries(false);
