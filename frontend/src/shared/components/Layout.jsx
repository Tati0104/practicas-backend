import { useState } from 'react';
import Sidebar from './Sidebar';
import Navbar from './Navbar';
import MockModeBanner from './MockModeBanner';

export default function Layout({ children }) {
  const [menuAbierto, setMenuAbierto] = useState(false);

  return (
    <div className="min-h-screen overflow-x-hidden ui-page">
      <Sidebar
        abierto={menuAbierto}
        onCerrar={() => setMenuAbierto(false)}
      />

      <div className="relative flex min-h-screen flex-col ui-page lg:pl-[17.5rem]">
        <MockModeBanner />
        <Navbar onAbrirMenu={() => setMenuAbierto(true)} />
        <main className="relative z-10 flex-1 ui-page p-4 sm:p-6 lg:-ml-5 lg:p-8 lg:pl-10">
          {children}
        </main>
      </div>
    </div>
  );
}
