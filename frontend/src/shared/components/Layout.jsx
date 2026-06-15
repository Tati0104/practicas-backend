import { useState } from 'react';
import Sidebar from './Sidebar';
import Navbar from './Navbar';
import MockModeBanner from './MockModeBanner';

export default function Layout({ children }) {
  const [menuAbierto, setMenuAbierto] = useState(false);

  return (
    <div className="min-h-screen overflow-x-hidden bg-white">
      <Sidebar
        abierto={menuAbierto}
        onCerrar={() => setMenuAbierto(false)}
      />

      <div className="relative flex min-h-screen flex-col bg-white lg:pl-[17.5rem]">
        <MockModeBanner />
        <Navbar onAbrirMenu={() => setMenuAbierto(true)} />
        {/* Solapa blanca sobre el borde del sidebar para unir con la pestaña activa */}
        <main className="relative z-10 flex-1 bg-white p-4 sm:p-6 lg:-ml-5 lg:p-8 lg:pl-10">
          {children}
        </main>
      </div>
    </div>
  );
}
