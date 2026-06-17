import { Routes, Route, Navigate, Link } from 'react-router-dom'
import { AuthProvider, useAuth } from './context/AuthContext'
import RotaProtegida from './components/RotaProtegida'
import LoginPage from './pages/LoginPage'
import CadastroPage from './pages/CadastroPage'
import CatalogoPage from './pages/CatalogoPage'
import EventoDetalhe from './pages/EventoDetalhe'
import MeusIngressos from './pages/MeusIngressos'
import SaldoPage from './pages/SaldoPage'
import GerenciarEventos from './pages/GerenciarEventos'
import RevendasPage from './pages/RevendasPage'
import RevisaoPedido from './pages/RevisaoPedido'
import RevenderIngressoPage from './pages/RevenderIngressoPage'
import SorteioPage from './pages/SorteioPage'
import GrupoCompraPage from './pages/GrupoCompraPage'
import MapaAssentosPage from './pages/MapaAssentosPage'
import CheckInPage from './pages/CheckInPage'
import DenunciasPage from './pages/DenunciasPage'
import AdminReembolsosPage from './pages/AdminReembolsosPage'
import AdminVisaoGeral from './pages/AdminVisaoGeral'
import AdminMarketplace from './pages/AdminMarketplace'
import RelatoriosPage from './pages/RelatoriosPage'
import DemoPage from './pages/DemoPage'
import { initMockMode, isMockMode } from './mocks/mockInterceptor'
import { FlaskConical } from 'lucide-react'

initMockMode()

function DemoBadge() {
  const { usuario } = useAuth()
  if (!isMockMode() || !usuario) return null
  return (
    <Link to="/demo" style={{ position: 'fixed', bottom: 72, right: 16, zIndex: 9999, display: 'flex', alignItems: 'center', gap: 6, background: 'oklch(0.30 0.05 250)', color: '#fff', borderRadius: 20, padding: '6px 14px', fontSize: '0.8125rem', fontWeight: 600, textDecoration: 'none', boxShadow: '0 2px 8px rgba(0,0,0,0.3)' }}>
      <FlaskConical size={14} /> DEMO · {usuario.nome.split(' ')[0]}
    </Link>
  )
}

export default function App() {
  return (
    <AuthProvider>
      <DemoBadge />
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/cadastro" element={<CadastroPage />} />

        <Route path="/" element={
          <RotaProtegida><CatalogoPage /></RotaProtegida>
        } />
        <Route path="/eventos/:id" element={
          <RotaProtegida><EventoDetalhe /></RotaProtegida>
        } />
        <Route path="/revisao" element={
          <RotaProtegida papelRequerido="COMPRADOR"><RevisaoPedido /></RotaProtegida>
        } />
        <Route path="/meus-ingressos" element={
          <RotaProtegida papelRequerido="COMPRADOR"><MeusIngressos /></RotaProtegida>
        } />
        <Route path="/saldo" element={
          <RotaProtegida papelRequerido="COMPRADOR"><SaldoPage /></RotaProtegida>
        } />
        <Route path="/gerenciar" element={
          <RotaProtegida papelRequerido="ORGANIZADOR"><GerenciarEventos /></RotaProtegida>
        } />
        <Route path="/revendas" element={
          <RotaProtegida papelRequerido="COMPRADOR"><RevendasPage /></RotaProtegida>
        } />
        <Route path="/revender/:id" element={
          <RotaProtegida papelRequerido="COMPRADOR"><RevenderIngressoPage /></RotaProtegida>
        } />
        <Route path="/sorteios" element={
          <RotaProtegida><SorteioPage /></RotaProtegida>
        } />
        <Route path="/grupos-compra" element={
          <RotaProtegida><GrupoCompraPage /></RotaProtegida>
        } />
        <Route path="/mapa-assentos" element={
          <RotaProtegida><MapaAssentosPage /></RotaProtegida>
        } />
        <Route path="/check-in" element={
          <RotaProtegida><CheckInPage /></RotaProtegida>
        } />
        <Route path="/admin" element={
          <RotaProtegida papelRequerido="ADMIN"><AdminVisaoGeral /></RotaProtegida>
        } />
        <Route path="/admin/marketplace" element={
          <RotaProtegida papelRequerido="ADMIN"><AdminMarketplace /></RotaProtegida>
        } />
        <Route path="/admin/relatorios" element={
          <RotaProtegida papelRequerido="ADMIN"><RelatoriosPage /></RotaProtegida>
        } />
        <Route path="/admin/reembolsos" element={
          <RotaProtegida papelRequerido="ADMIN"><AdminReembolsosPage /></RotaProtegida>
        } />
        <Route path="/denuncias" element={
          <RotaProtegida papelRequerido="ADMIN"><DenunciasPage /></RotaProtegida>
        } />
        <Route path="/demo" element={<DemoPage />} />

        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </AuthProvider>
  )
}
