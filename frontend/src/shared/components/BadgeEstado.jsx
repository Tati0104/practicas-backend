export default function BadgeEstado({ activo }) {
    return (
      <span style={{
        fontSize: 11, fontWeight: 600, padding: '3px 10px',
        borderRadius: 20, fontFamily: 'Arial, sans-serif',
        background: activo ? '#d1fae5' : '#fee2e2',
        color:      activo ? '#065f46' : '#991b1b'
      }}>
        {activo ? 'Activo' : 'Inactivo'}
      </span>
    );
  }