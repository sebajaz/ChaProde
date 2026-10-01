import React from 'react';
import { Trophy, Users, Calendar, ShieldCheck } from 'lucide-react';

export function App() {
  return (
    <div className="min-h-screen bg-slate-900 text-slate-100 p-8 font-sans">
      <header className="max-w-6xl mx-auto flex items-center justify-between border-b border-slate-800 pb-6 mb-8">
        <div className="flex items-center gap-3">
          <Trophy className="w-9 h-9 text-amber-400" />
          <div>
            <h1 className="text-2xl font-bold tracking-tight">ChaProde Backoffice</h1>
            <p className="text-sm text-slate-400">Panel de Administración de Torneos, Fixtures y Pronósticos</p>
          </div>
        </div>
        <span className="px-3 py-1 bg-amber-400/10 text-amber-400 border border-amber-400/20 text-xs font-semibold rounded-full flex items-center gap-1.5">
          <ShieldCheck className="w-3.5 h-3.5" /> Rol Admin
        </span>
      </header>

      <main className="max-w-6xl mx-auto grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="bg-slate-800/60 border border-slate-700/60 rounded-xl p-6 hover:border-slate-600 transition">
          <div className="flex items-center gap-4">
            <div className="p-3 bg-blue-500/10 text-blue-400 rounded-lg">
              <Trophy className="w-6 h-6" />
            </div>
            <div>
              <p className="text-sm text-slate-400">Torneos Activos</p>
              <h3 className="text-2xl font-bold">1</h3>
            </div>
          </div>
        </div>

        <div className="bg-slate-800/60 border border-slate-700/60 rounded-xl p-6 hover:border-slate-600 transition">
          <div className="flex items-center gap-4">
            <div className="p-3 bg-emerald-500/10 text-emerald-400 rounded-lg">
              <Calendar className="w-6 h-6" />
            </div>
            <div>
              <p className="text-sm text-slate-400">Partidos Programados</p>
              <h3 className="text-2xl font-bold">5</h3>
            </div>
          </div>
        </div>

        <div className="bg-slate-800/60 border border-slate-700/60 rounded-xl p-6 hover:border-slate-600 transition">
          <div className="flex items-center gap-4">
            <div className="p-3 bg-purple-500/10 text-purple-400 rounded-lg">
              <Users className="w-6 h-6" />
            </div>
            <div>
              <p className="text-sm text-slate-400">Usuarios Registrados</p>
              <h3 className="text-2xl font-bold">0</h3>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}

export default App;
