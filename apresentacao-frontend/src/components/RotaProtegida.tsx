import type { ReactNode } from 'react'
import { Navigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

interface Props {
  children: ReactNode
  papelRequerido?: 'ORGANIZADOR' | 'COMPRADOR' | 'ADMIN'
}

export default function RotaProtegida({ children, papelRequerido }: Props) {
  const { usuario } = useAuth()

  if (!usuario) {
    return <Navigate to="/login" replace />
  }

  if (papelRequerido && !usuario.papeis.includes(papelRequerido)) {
    return <Navigate to="/" replace />
  }

  return <>{children}</>
}
