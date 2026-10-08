import axios from 'axios';
import { ApiResponse, LeaderboardEntry, Match, MatchResultResponse, Tournament, User } from '../types';

const API_BASE_URL = 'http://localhost:8080/api';

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('chaprode_admin_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export const api = {
  // Autenticación
  login: async (usernameOrEmail: string, password: string): Promise<{ token: string; user: User }> => {
    const res = await apiClient.post<ApiResponse<{ token: string; user: User }>>('/auth/login', {
      usernameOrEmail,
      password,
    });
    if (res.data.success && res.data.data) {
      return res.data.data;
    }
    throw new Error(res.data.error || 'Error al iniciar sesión');
  },

  // Torneos
  getTournaments: async (): Promise<Tournament[]> => {
    const res = await apiClient.get<ApiResponse<Tournament[]>>('/torneos');
    return res.data.data || [];
  },

  getTournamentMatches: async (torneoId: string): Promise<Match[]> => {
    const res = await apiClient.get<ApiResponse<Match[]>>(`/torneos/${torneoId}/partidos`);
    return res.data.data || [];
  },

  seedTournaments: async (): Promise<Tournament[]> => {
    const res = await apiClient.post<ApiResponse<Tournament[]>>('/admin/torneos/seed');
    return res.data.data || [];
  },

  // Liquidación de Resultados
  settleMatchResult: async (
    matchId: string,
    golesLocal: number,
    golesVisitante: number,
    estado: string = 'FINALIZADO'
  ): Promise<MatchResultResponse> => {
    const res = await apiClient.post<ApiResponse<MatchResultResponse>>(`/admin/partidos/${matchId}/resultado`, {
      golesLocal,
      golesVisitante,
      estado,
    });
    if (res.data.success && res.data.data) {
      return res.data.data;
    }
    throw new Error(res.data.error || 'Error al liquidar el resultado');
  },

  // Ranking
  getTournamentLeaderboard: async (torneoId: string): Promise<LeaderboardEntry[]> => {
    const res = await apiClient.get<ApiResponse<{ ranking: LeaderboardEntry[] }>>(`/torneos/${torneoId}/ranking`);
    return res.data.data?.ranking || [];
  },

  getGlobalLeaderboard: async (): Promise<LeaderboardEntry[]> => {
    const res = await apiClient.get<ApiResponse<LeaderboardEntry[]>>('/ranking/global');
    return res.data.data || [];
  },

  // Sincronización Automática con API de Deportes (Opción 3 Híbrida)
  syncMatchesWithApi: async (modo?: 'live' | 'simular'): Promise<import('../types').SyncSummary> => {
    const url = modo ? `/admin/partidos/sincronizar?modo=${modo}` : '/admin/partidos/sincronizar';
    const res = await apiClient.post<ApiResponse<import('../types').SyncSummary>>(url);
    if (res.data.success && res.data.data) {
      return res.data.data;
    }
    throw new Error(res.data.error || 'Error al sincronizar con la API de deportes');
  },

  simulateMatch: async (partidoId: string, golesLocal: number, golesVisitante: number): Promise<MatchResultResponse> => {
    const res = await apiClient.post<ApiResponse<MatchResultResponse>>('/admin/partidos/simular', {
      partidoId,
      golesLocal,
      golesVisitante,
      estado: 'FINALIZADO',
    });
    if (res.data.success && res.data.data) {
      return res.data.data;
    }
    throw new Error(res.data.error || 'Error al simular el partido');
  },
};
