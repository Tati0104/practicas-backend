import { Badge } from './ui';

export default function BadgeEstado({ activo }) {
  return (
    <Badge variant={activo ? 'success' : 'danger'}>
      {activo ? 'Activo' : 'Inactivo'}
    </Badge>
  );
}
