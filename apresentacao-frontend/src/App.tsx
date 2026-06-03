import { Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
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

export default function App() {
  return (
    <AuthProvider>
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

        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </AuthProvider>
  )
}
