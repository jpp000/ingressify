import { Routes, Route } from 'react-router-dom'
import CatalogoPage from './pages/CatalogoPage'
import EventoDetalhe from './pages/EventoDetalhe'
import MeusIngressos from './pages/MeusIngressos'
import SaldoPage from './pages/SaldoPage'
import GerenciarEventos from './pages/GerenciarEventos'
import RevendasPage from './pages/RevendasPage'
import LoginPage from './pages/LoginPage'
import RevisaoPedido from './pages/RevisaoPedido'
import RevenderIngressoPage from './pages/RevenderIngressoPage'

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/" element={<CatalogoPage />} />
      <Route path="/eventos/:id" element={<EventoDetalhe />} />
      <Route path="/revisao" element={<RevisaoPedido />} />
      <Route path="/meus-ingressos" element={<MeusIngressos />} />
      <Route path="/saldo" element={<SaldoPage />} />
      <Route path="/gerenciar" element={<GerenciarEventos />} />
      <Route path="/revendas" element={<RevendasPage />} />
      <Route path="/revender/:id" element={<RevenderIngressoPage />} />
    </Routes>
  )
}
