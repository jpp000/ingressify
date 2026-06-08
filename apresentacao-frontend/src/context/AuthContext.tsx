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
  isOperadorPorta: () => boolean
  isAdmin: () => boolean
}

const AuthContext = createContext<AuthContextValue | null>(null)

const STORAGE_KEY = 'ingressify_usuario'

export function AuthProvider({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<UsuarioLogado | null>(() => {
    try {
      const saved = sessionStorage.getItem(STORAGE_KEY)
      if (!saved) return null
      const parsed = JSON.parse(saved) as UsuarioLogado
      return {
        ...parsed,
        papeis: Array.isArray(parsed.papeis) ? parsed.papeis : Object.values(parsed.papeis ?? {}),
      }
    } catch {
      return null
    }
  })

  const login = (u: UsuarioLogado) => {
    const normalizado = {
      ...u,
      papeis: Array.isArray(u.papeis) ? u.papeis : Object.values(u.papeis ?? {}),
    }
    setUsuario(normalizado)
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(normalizado))
  }

  const logout = () => {
    setUsuario(null)
    sessionStorage.removeItem(STORAGE_KEY)
  }

  const isOrganizador = () => usuario?.papeis?.includes('ORGANIZADOR') ?? false
  const isComprador = () => usuario?.papeis?.includes('COMPRADOR') ?? false
  const isOperadorPorta = () => usuario?.papeis?.includes('OPERADOR_PORTA') ?? false
  const isAdmin = () => usuario?.papeis?.includes('ADMIN') ?? false

  return (
    <AuthContext.Provider value={{ usuario, login, logout, isOrganizador, isComprador, isOperadorPorta, isAdmin }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth precisa estar dentro de AuthProvider')
  return ctx
}
