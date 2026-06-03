const ROLES = ['ADMIN','DIRECCION','COORD_ACADEMICA','COORD_PRACTICA',
    'SECRETARIA','DOCENTE_ASESOR','EMPRESA','TUTOR_EMPRESARIAL','ESTUDIANTE'];

export default function FiltrosUsuario({ filtros, onChange }) {
return (
<div style={estilos.contenedor}>
<select
value={filtros.rol || ''}
onChange={e => onChange({ ...filtros, rol: e.target.value || undefined })}
style={estilos.select}
>
<option value="">Todos los roles</option>
{ROLES.map(r => <option key={r} value={r}>{r}</option>)}
</select>

<select
value={filtros.activo ?? ''}
onChange={e => onChange({ ...filtros, activo: e.target.value === '' ? undefined : e.target.value === 'true' })}
style={estilos.select}
>
<option value="">Todos los estados</option>
<option value="true">Activos</option>
<option value="false">Inactivos</option>
</select>
</div>
);
}

const estilos = {
contenedor: { display: 'flex', gap: 10, marginBottom: 16, flexWrap: 'wrap' },
select: {
padding: '8px 12px', border: '1px solid #d1d5db',
borderRadius: 8, fontSize: 13, color: '#374151',
background: '#fff', cursor: 'pointer'
}
};