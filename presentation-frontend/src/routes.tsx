import { Navigate, Route, Routes } from "react-router-dom";
import { EventosCatalogo } from "./pages/EventosCatalogo";

export function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/eventos" replace />} />
      <Route path="/eventos" element={<EventosCatalogo />} />
    </Routes>
  );
}
