import { Routes, Route, Link } from 'react-router-dom'
import CatalogoPage from './pages/CatalogoPage'
import EventoDetalhe from './pages/EventoDetalhe'
import MeusIngressos from './pages/MeusIngressos'
import SaldoPage from './pages/SaldoPage'
import GerenciarEventos from './pages/GerenciarEventos'
import RevendasPage from './pages/RevendasPage'

export default function App() {
  return (
    <div style={{ fontFamily: 'sans-serif', maxWidth: 1200, margin: '0 auto', padding: 16 }}>
      <header style={{ display: 'flex', gap: 24, padding: '12px 0', borderBottom: '1px solid #ddd', marginBottom: 24 }}>
        <strong style={{ fontSize: 20 }}>Ingressify</strong>
        <nav style={{ display: 'flex', gap: 16, alignItems: 'center' }}>
          <Link to="/">Catálogo</Link>
          <Link to="/meus-ingressos">Meus Ingressos</Link>
          <Link to="/revendas">Revendas</Link>
          <Link to="/saldo">Saldo</Link>
          <Link to="/gerenciar">Gerenciar Eventos</Link>
        </nav>
      </header>
      <main>
        <Routes>
          <Route path="/" element={<CatalogoPage />} />
          <Route path="/eventos/:id" element={<EventoDetalhe />} />
          <Route path="/meus-ingressos" element={<MeusIngressos />} />
          <Route path="/saldo" element={<SaldoPage />} />
          <Route path="/gerenciar" element={<GerenciarEventos />} />
          <Route path="/revendas" element={<RevendasPage />} />
        </Routes>
      </main>
    </div>
  )
}
