export interface User {
  id: string;
  username: string;
  email: string;
  rol: 'ADMIN' | 'USER';
  createdAt?: string;
}

export interface Team {
  id: string;
  nombre: string;
  codigoExterno: string | null;
  urlBandera: string;
  codigoIso: string | null;
}

export interface Match {
  id: string;
  torneoId: string;
  equipoLocal: Team;
  equipoVisitante: Team;
  fechaPartido: string;
  golesLocal: number | null;
  golesVisitante: number | null;
  estado: 'PENDIENTE' | 'EN_JUEGO' | 'FINALIZADO';
  codigoExterno: string | null;
}

export interface Tournament {
  id: string;
  nombre: string;
  codigoExterno: string | null;
  fechaInicio: string;
  fechaFin: string;
  activo: boolean;
  totalEquipos: number;
  totalPartidos: number;
}

export interface LeaderboardEntry {
  posicion: number;
  usuarioId: string;
  username: string;
  puntosTotales: number;
  plenosExactos: number;
  aciertosTendencia: number;
  pronosticosTotales: number;
}

export interface League {
  id: string;
  nombre: string;
  codigoAcceso: string;
  creadorId: string;
  creadorUsername: string;
  torneoId: string;
  torneoNombre: string;
  totalMiembros: number;
  createdAt: string;
}

export interface MatchResultResponse {
  partidoId: string;
  golesLocal: number;
  golesVisitante: number;
  estado: string;
  totalPronosticosLiquidados: number;
  totalPuntosOtorgados: number;
}

export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  error?: string;
  message?: string;
}
