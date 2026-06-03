import { createContext, useContext, useState, type ReactNode } from 'react'

export interface UsuarioLogado {
  id: number
  nome: string
  email: string
  papeis: string[]
}

interface AuthContextValue {
  usuario: UsuarioLogado | null
  login: (u: UsuarioLogado) => void
  logout: () => void
  isOrganizador: () => boolean
  isComprador: () => boolean
}

const AuthContext = createContext<AuthContextValue | null>(null)

const STORAGE_KEY = 'ingressify_usuario'

export function AuthProvider({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<UsuarioLogado | null>(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEY)
      return saved ? (JSON.parse(saved) as UsuarioLogado) : null
    } catch {
      return null
    }
  })

  const login = (u: UsuarioLogado) => {
    setUsuario(u)
    localStorage.setItem(STORAGE_KEY, JSON.stringify(u))
  }

  const logout = () => {
    setUsuario(null)
    localStorage.removeItem(STORAGE_KEY)
  }

  const isOrganizador = () => usuario?.papeis?.includes('ORGANIZADOR') ?? false
  const isComprador = () => usuario?.papeis?.includes('COMPRADOR') ?? false

  return (
    <AuthContext.Provider value={{ usuario, login, logout, isOrganizador, isComprador }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth precisa estar dentro de AuthProvider')
  return ctx
}
