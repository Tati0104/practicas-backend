import Sidebar from './Sidebar';
import Navbar  from './Navbar';

export default function Layout({ children }) {
  return (
    <div style={estilos.contenedor}>
      <Sidebar />
      <div style={estilos.principal}>
        <Navbar />
        <main style={estilos.main}>
          {children}
        </main>
      </div>
    </div>
  );
}

const estilos = {
  contenedor: {
    display: 'flex',
    minHeight: '100vh',
    background: '#f8fafc',
    fontFamily: 'Arial, sans-serif'
  },
  principal: {
    marginLeft: 220,
    flex: 1,
    display: 'flex',
    flexDirection: 'column'
  },
  main: {
    padding: 28,
    flex: 1
  }
};