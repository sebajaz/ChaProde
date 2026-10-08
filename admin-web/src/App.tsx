import React, { useState, useEffect } from 'react';
import { 
  Trophy, 
  Users, 
  Calendar, 
  ShieldCheck, 
  Play, 
  CheckCircle2, 
  Clock, 
  RefreshCw, 
  LogIn, 
  LogOut,
  ChevronRight,
  AlertCircle,
  Sparkles
} from 'lucide-react';
import { api } from './services/api';
import { Match, Tournament, LeaderboardEntry, User } from './types';

export function App() {
  const [user, setUser] = useState<User | null>(null);
  const [activeTab, setActiveTab] = useState<'dashboard' | 'fixture' | 'ranking'>('dashboard');
  const [tournaments, setTournaments] = useState<Tournament[]>([]);
  const [selectedTournament, setSelectedTournament] = useState<Tournament | null>(null);
  const [matches, setMatches] = useState<Match[]>([]);
  const [leaderboard, setLeaderboard] = useState<LeaderboardEntry[]>([]);
  const [loading, setLoading] = useState(false);
  const [actionMessage, setActionMessage] = useState<{ text: string; type: 'success' | 'error' } | null>(null);

  // Modal de Login
  const [showLoginModal, setShowLoginModal] = useState(false);
  const [loginIdentifier, setLoginIdentifier] = useState('admin');
  const [loginPassword, setLoginPassword] = useState('prodepw');
  const [loginError, setLoginError] = useState<string | null>(null);

  // Modal de Carga de Resultado
  const [selectedMatch, setSelectedMatch] = useState<Match | null>(null);
  const [inputLocalGoals, setInputLocalGoals] = useState(0);
  const [inputVisitorGoals, setInputVisitorGoals] = useState(0);
  const [matchStatus, setMatchStatus] = useState<string>('FINALIZADO');
  const [isSettling, setIsSettling] = useState(false);

  useEffect(() => {
    const savedUser = localStorage.getItem('chaprode_admin_user');
    if (savedUser) {
      try {
        setUser(JSON.parse(savedUser));
      } catch (e) {}
    }
    loadInitialData();
  }, []);

  const loadInitialData = async () => {
    try {
      setLoading(true);
      const tourns = await api.getTournaments();
      setTournaments(tourns);
      if (tourns.length > 0) {
        setSelectedTournament(tourns[0]);
        const matchData = await api.getTournamentMatches(tourns[0].id);
        setMatches(matchData);
        const rankData = await api.getTournamentLeaderboard(tourns[0].id);
        setLeaderboard(rankData);
      }
    } catch (err: any) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoginError(null);
    try {
      const data = await api.login(loginIdentifier, loginPassword);
      if (data.user.rol !== 'ADMIN') {
        setLoginError('Esta cuenta no posee privilegios de Administrador.');
        return;
      }
      localStorage.setItem('chaprode_admin_token', data.token);
      localStorage.setItem('chaprode_admin_user', JSON.stringify(data.user));
      setUser(data.user);
      setShowLoginModal(false);
      setActionMessage({ text: `Bienvenido, Administrador ${data.user.username}`, type: 'success' });
      loadInitialData();
    } catch (err: any) {
      setLoginError(err.message || 'Error al autenticarse');
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('chaprode_admin_token');
    localStorage.removeItem('chaprode_admin_user');
    setUser(null);
    setActionMessage({ text: 'Sesión cerrada', type: 'success' });
  };

  const handleSeedData = async () => {
    try {
      setLoading(true);
      await api.seedTournaments();
      setActionMessage({ text: 'Datos semilla del Mundial 2026 importados correctamente.', type: 'success' });
      loadInitialData();
    } catch (err: any) {
      setActionMessage({ text: err.message || 'Error al importar datos semilla', type: 'error' });
    } finally {
      setLoading(false);
    }
  };

  const openSettleModal = (match: Match) => {
    setSelectedMatch(match);
    setInputLocalGoals(match.golesLocal ?? 0);
    setInputVisitorGoals(match.golesVisitante ?? 0);
    setMatchStatus(match.estado === 'PENDIENTE' ? 'FINALIZADO' : match.estado);
  };

  const handleSettleMatch = async () => {
    if (!selectedMatch) return;
    try {
      setIsSettling(true);
      const res = await api.settleMatchResult(
        selectedMatch.id,
        inputLocalGoals,
        inputVisitorGoals,
        matchStatus
      );
      setActionMessage({
        text: `¡Resultado registrado! Se liquidaron ${res.totalPronosticosLiquidados} pronósticos y se otorgaron ${res.totalPuntosOtorgados} puntos.`,
        type: 'success'
      });
      setSelectedMatch(null);
      // Refrescar fixture y ranking
      if (selectedTournament) {
        const updatedMatches = await api.getTournamentMatches(selectedTournament.id);
        setMatches(updatedMatches);
        const updatedRanking = await api.getTournamentLeaderboard(selectedTournament.id);
        setLeaderboard(updatedRanking);
      }
    } catch (err: any) {
      setActionMessage({ text: err.message || 'Error al liquidar el partido', type: 'error' });
    } finally {
      setIsSettling(false);
    }
  };

  const finishedMatchesCount = matches.filter(m => m.estado === 'FINALIZADO').length;
  const pendingMatchesCount = matches.filter(m => m.estado === 'PENDIENTE').length;

  return (
    <div className="min-h-screen bg-slate-900 text-slate-100 flex flex-col font-sans">
      {/* HEADER SUPERIOR */}
      <header className="bg-slate-950/80 backdrop-blur border-b border-slate-800 sticky top-0 z-30">
        <div className="max-w-7xl mx-auto px-6 h-18 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="p-2 bg-amber-400/10 rounded-xl border border-amber-400/20">
              <Trophy className="w-7 h-7 text-amber-400" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <span className="font-black text-xl tracking-wide bg-gradient-to-r from-amber-400 to-sky-400 bg-clip-text text-transparent">
                  ChaProde
                </span>
                <span className="text-xs px-2 py-0.5 bg-slate-800 text-slate-400 font-semibold rounded-md border border-slate-700">
                  Backoffice v1.0
                </span>
              </div>
              <p className="text-xs text-slate-400">Portal de Operaciones y Liquidación de Partidos</p>
            </div>
          </div>

          <div className="flex items-center gap-4">
            {user ? (
              <div className="flex items-center gap-3 bg-slate-900 border border-slate-800 px-3 py-1.5 rounded-xl">
                <span className="w-2.5 h-2.5 bg-emerald-400 rounded-full animate-pulse"></span>
                <div className="text-left text-xs">
                  <p className="font-bold text-slate-200">{user.username}</p>
                  <p className="text-amber-400 font-semibold uppercase">{user.rol}</p>
                </div>
                <button
                  onClick={handleLogout}
                  title="Cerrar Sesión"
                  className="ml-2 p-1.5 hover:bg-slate-800 rounded-lg text-slate-400 hover:text-red-400 transition"
                >
                  <LogOut className="w-4 h-4" />
                </button>
              </div>
            ) : (
              <button
                onClick={() => setShowLoginModal(true)}
                className="flex items-center gap-2 bg-sky-600 hover:bg-sky-500 text-white font-semibold text-xs px-4 py-2 rounded-xl transition shadow-lg shadow-sky-600/20"
              >
                <LogIn className="w-4 h-4" /> Iniciar Sesión Admin
              </button>
            )}
          </div>
        </div>

        {/* NAVEGACIÓN POR PESTAÑAS */}
        <div className="max-w-7xl mx-auto px-6 flex gap-2 border-t border-slate-800/60 pt-1">
          <button
            onClick={() => setActiveTab('dashboard')}
            className={`px-4 py-2.5 text-xs font-bold transition border-b-2 flex items-center gap-2 ${
              activeTab === 'dashboard'
                ? 'border-sky-400 text-sky-400 bg-sky-500/5'
                : 'border-transparent text-slate-400 hover:text-slate-200'
            }`}
          >
            📊 Resumen & Métricas
          </button>
          <button
            onClick={() => setActiveTab('fixture')}
            className={`px-4 py-2.5 text-xs font-bold transition border-b-2 flex items-center gap-2 ${
              activeTab === 'fixture'
                ? 'border-sky-400 text-sky-400 bg-sky-500/5'
                : 'border-transparent text-slate-400 hover:text-slate-200'
            }`}
          >
            ⚽ Fixture & Carga de Resultados
          </button>
          <button
            onClick={() => setActiveTab('ranking')}
            className={`px-4 py-2.5 text-xs font-bold transition border-b-2 flex items-center gap-2 ${
              activeTab === 'ranking'
                ? 'border-sky-400 text-sky-400 bg-sky-500/5'
                : 'border-transparent text-slate-400 hover:text-slate-200'
            }`}
          >
            🏆 Tabla de Posiciones
          </button>
        </div>
      </header>

      {/* MENSAJE DE ESTADO / TOAST */}
      {actionMessage && (
        <div className="max-w-7xl mx-auto w-full px-6 mt-4">
          <div
            className={`p-3.5 rounded-xl border text-xs font-semibold flex items-center justify-between ${
              actionMessage.type === 'success'
                ? 'bg-emerald-500/10 border-emerald-500/30 text-emerald-400'
                : 'bg-red-500/10 border-red-500/30 text-red-400'
            }`}
          >
            <span>{actionMessage.text}</span>
            <button onClick={() => setActionMessage(null)} className="opacity-60 hover:opacity-100">✕</button>
          </div>
        </div>
      )}

      {/* CONTENIDO PRINCIPAL */}
      <main className="max-w-7xl mx-auto w-full px-6 py-8 flex-1">
        {/* PESTAÑA 1: DASHBOARD */}
        {activeTab === 'dashboard' && (
          <div className="space-y-8">
            <div className="grid grid-cols-1 md:grid-cols-4 gap-5">
              <div className="bg-slate-800/60 border border-slate-700/60 rounded-2xl p-5">
                <div className="flex items-center gap-3">
                  <div className="p-3 bg-amber-400/10 text-amber-400 rounded-xl">
                    <Trophy className="w-5 h-5" />
                  </div>
                  <div>
                    <p className="text-xs text-slate-400 font-medium">Torneos Activos</p>
                    <h3 className="text-2xl font-black text-slate-100">{tournaments.length}</h3>
                  </div>
                </div>
              </div>

              <div className="bg-slate-800/60 border border-slate-700/60 rounded-2xl p-5">
                <div className="flex items-center gap-3">
                  <div className="p-3 bg-sky-400/10 text-sky-400 rounded-xl">
                    <Clock className="w-5 h-5" />
                  </div>
                  <div>
                    <p className="text-xs text-slate-400 font-medium">Partidos Pendientes</p>
                    <h3 className="text-2xl font-black text-slate-100">{pendingMatchesCount}</h3>
                  </div>
                </div>
              </div>

              <div className="bg-slate-800/60 border border-slate-700/60 rounded-2xl p-5">
                <div className="flex items-center gap-3">
                  <div className="p-3 bg-emerald-400/10 text-emerald-400 rounded-xl">
                    <CheckCircle2 className="w-5 h-5" />
                  </div>
                  <div>
                    <p className="text-xs text-slate-400 font-medium">Partidos Liquidados</p>
                    <h3 className="text-2xl font-black text-slate-100">{finishedMatchesCount}</h3>
                  </div>
                </div>
              </div>

              <div className="bg-slate-800/60 border border-slate-700/60 rounded-2xl p-5">
                <div className="flex items-center gap-3">
                  <div className="p-3 bg-purple-400/10 text-purple-400 rounded-xl">
                    <Users className="w-5 h-5" />
                  </div>
                  <div>
                    <p className="text-xs text-slate-400 font-medium">Participantes en Ranking</p>
                    <h3 className="text-2xl font-black text-slate-100">{leaderboard.length}</h3>
                  </div>
                </div>
              </div>
            </div>

            {/* BANNER DE ACCIONES */}
            <div className="bg-gradient-to-r from-slate-800/80 to-slate-850/80 border border-slate-700/60 rounded-2xl p-6 flex flex-col md:flex-row items-center justify-between gap-4">
              <div>
                <h4 className="text-base font-bold text-slate-100 flex items-center gap-2">
                  <Sparkles className="w-4 h-4 text-amber-400" /> Sincronización del Mundial FIFA 2026
                </h4>
                <p className="text-xs text-slate-400 mt-1">
                  Reinicia o sincroniza los 8 equipos clasificados y los 5 partidos iniciales de prueba en la base de datos.
                </p>
              </div>
              <button
                onClick={handleSeedData}
                disabled={loading}
                className="flex items-center gap-2 bg-slate-700 hover:bg-slate-600 text-white font-semibold text-xs px-4 py-2.5 rounded-xl transition border border-slate-600"
              >
                <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} /> Re-sembrar Datos
              </button>
            </div>
          </div>
        )}

        {/* PESTAÑA 2: FIXTURE & CARGA DE RESULTADOS */}
        {activeTab === 'fixture' && (
          <div className="space-y-6">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-lg font-bold text-slate-100">Fixture de Partidos</h3>
                <p className="text-xs text-slate-400">
                  Ingresa los marcadores oficiales. Al guardar, el motor liquidará automáticamente los pronósticos (3, 1 o 0 puntos).
                </p>
              </div>
            </div>

            <div className="bg-slate-800/50 border border-slate-700/60 rounded-2xl overflow-hidden shadow-xl">
              <table className="w-full text-left border-collapse text-xs">
                <thead>
                  <tr className="bg-slate-850 border-b border-slate-700/80 text-slate-400 uppercase tracking-wider text-[11px]">
                    <th className="py-3.5 px-4">Fecha & Hora</th>
                    <th className="py-3.5 px-4 text-right">Equipo Local</th>
                    <th className="py-3.5 px-4 text-center">Resultado</th>
                    <th className="py-3.5 px-4">Equipo Visitante</th>
                    <th className="py-3.5 px-4 text-center">Estado</th>
                    <th className="py-3.5 px-4 text-right">Acción</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-800">
                  {matches.map((m) => (
                    <tr key={m.id} className="hover:bg-slate-800/40 transition">
                      <td className="py-4 px-4 text-slate-400 whitespace-nowrap">
                        {m.fechaPartido.replace('T', ' ').replace('Z', ' UTC')}
                      </td>
                      <td className="py-4 px-4 text-right font-bold text-slate-200">
                        {m.equipoLocal.nombre}
                      </td>
                      <td className="py-4 px-4 text-center">
                        {m.golesLocal !== null && m.golesVisitante !== null ? (
                          <span className="font-mono text-sm font-black bg-slate-900 border border-slate-700 px-3 py-1 rounded-lg text-amber-400">
                            {m.golesLocal} - {m.golesVisitante}
                          </span>
                        ) : (
                          <span className="text-slate-500 font-bold">VS</span>
                        )}
                      </td>
                      <td className="py-4 px-4 font-bold text-slate-200">
                        {m.equipoVisitante.nombre}
                      </td>
                      <td className="py-4 px-4 text-center">
                        <span
                          className={`px-2.5 py-1 rounded-full text-[10px] font-bold uppercase tracking-wide border ${
                            m.estado === 'FINALIZADO'
                              ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20'
                              : m.estado === 'EN_JUEGO'
                              ? 'bg-red-500/10 text-red-400 border-red-500/20 animate-pulse'
                              : 'bg-sky-500/10 text-sky-400 border-sky-500/20'
                          }`}
                        >
                          {m.estado}
                        </span>
                      </td>
                      <td className="py-4 px-4 text-right">
                        <button
                          onClick={() => openSettleModal(m)}
                          className="bg-sky-600 hover:bg-sky-500 text-white font-bold px-3 py-1.5 rounded-lg text-xs transition shadow-sm"
                        >
                          {m.estado === 'FINALIZADO' ? 'Modificar' : 'Cargar Resultado'}
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* PESTAÑA 3: RANKING */}
        {activeTab === 'ranking' && (
          <div className="space-y-6">
            <div>
              <h3 className="text-lg font-bold text-slate-100">Tabla de Posiciones Oficial</h3>
              <p className="text-xs text-slate-400">
                Puntuaciones consolidadas del torneo calculadas automáticamente tras cada resultado liquidado.
              </p>
            </div>

            <div className="bg-slate-800/50 border border-slate-700/60 rounded-2xl overflow-hidden shadow-xl">
              <table className="w-full text-left border-collapse text-xs">
                <thead>
                  <tr className="bg-slate-850 border-b border-slate-700/80 text-slate-400 uppercase tracking-wider text-[11px]">
                    <th className="py-3.5 px-4 text-center w-16">Pos</th>
                    <th className="py-3.5 px-4">Usuario</th>
                    <th className="py-3.5 px-4 text-center">Plenos (3 pts)</th>
                    <th className="py-3.5 px-4 text-center">Aciertos (1 pt)</th>
                    <th className="py-3.5 px-4 text-center">Total Pronósticos</th>
                    <th className="py-3.5 px-4 text-right">Puntos Totales</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-800">
                  {leaderboard.length === 0 ? (
                    <tr>
                      <td colSpan={6} className="text-center py-8 text-slate-500">
                        Aún no hay usuarios con puntos liquidados en este torneo.
                      </td>
                    </tr>
                  ) : (
                    leaderboard.map((row) => (
                      <tr key={row.usuarioId} className="hover:bg-slate-800/40 transition">
                        <td className="py-3.5 px-4 text-center font-bold text-amber-400">
                          #{row.posicion}
                        </td>
                        <td className="py-3.5 px-4 font-bold text-slate-200">
                          {row.username}
                        </td>
                        <td className="py-3.5 px-4 text-center text-emerald-400 font-semibold">
                          {row.plenosExactos}
                        </td>
                        <td className="py-3.5 px-4 text-center text-sky-400 font-semibold">
                          {row.aciertosTendencia}
                        </td>
                        <td className="py-3.5 px-4 text-center text-slate-400">
                          {row.pronosticosTotales}
                        </td>
                        <td className="py-3.5 px-4 text-right font-black text-amber-400 text-sm">
                          {row.puntosTotales} pts
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </main>

      {/* MODAL DE CARGA DE RESULTADO */}
      {selectedMatch && (
        <div className="fixed inset-0 bg-black/70 backdrop-blur-sm flex items-center justify-center p-4 z-50">
          <div className="bg-slate-850 border border-slate-700 rounded-2xl max-w-md w-full p-6 shadow-2xl">
            <h4 className="text-base font-bold text-slate-100 flex items-center gap-2">
              <Trophy className="w-5 h-5 text-amber-400" /> Cargar Resultado Oficial
            </h4>
            <p className="text-xs text-slate-400 mt-1">
              Ingresa los goles del encuentro para disparar la liquidación automática de puntos.
            </p>

            <div className="my-6 bg-slate-900 border border-slate-800 rounded-xl p-4 flex items-center justify-between gap-4">
              <div className="text-center flex-1">
                <p className="text-xs font-bold text-slate-300 mb-2">{selectedMatch.equipoLocal.nombre}</p>
                <input
                  type="number"
                  min="0"
                  max="99"
                  value={inputLocalGoals}
                  onChange={(e) => setInputLocalGoals(parseInt(e.target.value) || 0)}
                  className="w-16 h-14 bg-slate-800 border border-slate-700 rounded-xl text-center text-2xl font-black text-amber-400 focus:outline-none focus:border-sky-500"
                />
              </div>

              <span className="text-slate-600 font-black text-xl">-</span>

              <div className="text-center flex-1">
                <p className="text-xs font-bold text-slate-300 mb-2">{selectedMatch.equipoVisitante.nombre}</p>
                <input
                  type="number"
                  min="0"
                  max="99"
                  value={inputVisitorGoals}
                  onChange={(e) => setInputVisitorGoals(parseInt(e.target.value) || 0)}
                  className="w-16 h-14 bg-slate-800 border border-slate-700 rounded-xl text-center text-2xl font-black text-amber-400 focus:outline-none focus:border-sky-500"
                />
              </div>
            </div>

            <div className="mb-6">
              <label className="block text-xs font-semibold text-slate-400 mb-1.5">Estado del Partido</label>
              <select
                value={matchStatus}
                onChange={(e) => setMatchStatus(e.target.value)}
                className="w-full bg-slate-900 border border-slate-700 rounded-xl p-2.5 text-xs text-slate-200 font-semibold focus:outline-none focus:border-sky-500"
              >
                <option value="FINALIZADO">FINALIZADO (Liquidar Puntos)</option>
                <option value="EN_JUEGO">EN JUEGO (Marcador en vivo)</option>
              </select>
            </div>

            <div className="flex gap-3">
              <button
                onClick={() => setSelectedMatch(null)}
                className="flex-1 bg-slate-800 hover:bg-slate-700 text-slate-300 font-bold text-xs py-2.5 rounded-xl transition"
              >
                Cancelar
              </button>
              <button
                onClick={handleSettleMatch}
                disabled={isSettling}
                className="flex-1 bg-sky-600 hover:bg-sky-500 text-white font-bold text-xs py-2.5 rounded-xl transition shadow-lg shadow-sky-600/20"
              >
                {isSettling ? 'Liquidando...' : 'Guardar y Liquidar'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* MODAL DE LOGIN ADMIN */}
      {showLoginModal && (
        <div className="fixed inset-0 bg-black/70 backdrop-blur-sm flex items-center justify-center p-4 z-50">
          <form onSubmit={handleLogin} className="bg-slate-850 border border-slate-700 rounded-2xl max-w-sm w-full p-6 shadow-2xl">
            <h4 className="text-base font-bold text-slate-100 flex items-center gap-2">
              <ShieldCheck className="w-5 h-5 text-amber-400" /> Iniciar Sesión de Administrador
            </h4>
            <p className="text-xs text-slate-400 mt-1 mb-4">
              Ingresa con tu usuario o email con privilegios de ADMIN.
            </p>

            {loginError && (
              <div className="p-3 bg-red-500/10 border border-red-500/30 rounded-xl text-red-400 text-xs font-semibold mb-4">
                {loginError}
              </div>
            )}

            <div className="space-y-3 mb-6">
              <div>
                <label className="block text-xs font-semibold text-slate-400 mb-1">Usuario o Email</label>
                <input
                  type="text"
                  value={loginIdentifier}
                  onChange={(e) => setLoginIdentifier(e.target.value)}
                  className="w-full bg-slate-900 border border-slate-700 rounded-xl p-2.5 text-xs text-slate-100 focus:outline-none focus:border-sky-500"
                  required
                />
              </div>
              <div>
                <label className="block text-xs font-semibold text-slate-400 mb-1">Contraseña</label>
                <input
                  type="password"
                  value={loginPassword}
                  onChange={(e) => setLoginPassword(e.target.value)}
                  className="w-full bg-slate-900 border border-slate-700 rounded-xl p-2.5 text-xs text-slate-100 focus:outline-none focus:border-sky-500"
                  required
                />
              </div>
            </div>

            <div className="flex gap-3">
              <button
                type="button"
                onClick={() => setShowLoginModal(false)}
                className="flex-1 bg-slate-800 hover:bg-slate-700 text-slate-300 font-bold text-xs py-2.5 rounded-xl transition"
              >
                Cancelar
              </button>
              <button
                type="submit"
                className="flex-1 bg-sky-600 hover:bg-sky-500 text-white font-bold text-xs py-2.5 rounded-xl transition shadow-lg shadow-sky-600/20"
              >
                Ingresar
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
}

export default App;
