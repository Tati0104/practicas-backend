import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import FiltrosBar from './FiltrosBar';
import FiltrosBotones from './FiltrosBotones';

/**
 * Formulario de filtros con RHF + Zod. Al enviar resetea page a 0 vía onAplicar.
 */
export default function FiltrosFormulario({
  schema,
  valoresIniciales,
  onAplicar,
  onLimpiar,
  variant = 'card',
  children,
}) {
  const { register, handleSubmit, reset } = useForm({
    resolver: zodResolver(schema),
    defaultValues: valoresIniciales,
  });

  const submit = (data) => onAplicar(data);

  const limpiar = () => {
    const vacios = Object.fromEntries(Object.keys(valoresIniciales).map((k) => [k, '']));
    reset(vacios);
    onLimpiar?.(vacios);
  };

  return (
    <FiltrosBar as="form" variant={variant} onSubmit={handleSubmit(submit)}>
      {typeof children === 'function' ? children({ register }) : children}
      <FiltrosBotones onLimpiar={limpiar} />
    </FiltrosBar>
  );
}
