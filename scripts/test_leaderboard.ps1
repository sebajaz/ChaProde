# ==============================================================================
# ChaProde - Script de Testing Automatizado para Tabla de Posiciones / Ranking
# ==============================================================================
# 1. Registra/Autentica 10 usuarios con perfiles diversos (desde el lider al ultimo).
# 2. Crea 5 partidos de prueba de alto nivel con fecha futura.
# 3. Carga pronosticos variados para cada uno de los 10 usuarios.
# 4. Inicia los partidos (estado EN_JUEGO) y liquida los resultados oficiales.
# 5. Consulta y renderiza la Tabla de Posiciones Oficial en consola.
# ==============================================================================

param (
    [string]$BaseUrl = "http://localhost:8080"
)

$ErrorActionPreference = "Stop"

Write-Host ""
Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host "  ChaProde - Testing de Tabla de Posiciones (10 Usuarios)         " -ForegroundColor Yellow
Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host "Servidor objetivo: $BaseUrl" -ForegroundColor Gray
Write-Host ""

# 1. Verificar salud del backend
try {
    $health = Invoke-RestMethod -Uri "$BaseUrl/health" -Method Get -TimeoutSec 5
    Write-Host "[OK] Backend en linea (Status: $($health.data.status))" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] No se pudo conectar al backend en $BaseUrl." -ForegroundColor Red
    Write-Host "Asegurate de que Docker este corriendo con: docker compose up -d" -ForegroundColor Yellow
    exit 1
}

# 2. Obtener torneo y equipos existentes
$torneos = (Invoke-RestMethod -Uri "$BaseUrl/api/torneos" -Method Get).data
if (-not $torneos -or $torneos.Count -eq 0) {
    Write-Host "[ERROR] No hay torneos registrados. Ejecuta primero la siembra de ligas." -ForegroundColor Red
    exit 1
}
$torneo = $torneos[0]
Write-Host "[OK] Torneo seleccionado: $($torneo.nombre) (ID: $($torneo.id))" -ForegroundColor DarkCyan

$equipos = (Invoke-RestMethod -Uri "$BaseUrl/api/equipos" -Method Get).data
if ($equipos.Count -lt 2) {
    Write-Host "[ERROR] Se requieren al menos 2 equipos en la base de datos." -ForegroundColor Red
    exit 1
}

# 3. Definicion de los 10 usuarios de prueba y sus pronosticos
$usuariosDef = @(
    @{ Username = "Leo_El10";         Email = "leo10@test.com";         Rol = "Lider (Puntero)";    Preds = @(@{L=2;V=1}, @{L=3;V=2}, @{L=1;V=1}, @{L=0;V=2}, @{L=2;V=2}) },
    @{ Username = "Kun_Pichichi";     Email = "kun@test.com";           Rol = "Subcampeon";         Preds = @(@{L=2;V=1}, @{L=2;V=1}, @{L=1;V=1}, @{L=0;V=2}, @{L=1;V=0}) },
    @{ Username = "Fideo_DiMaria";    Email = "fideo@test.com";         Rol = "Top 3";              Preds = @(@{L=1;V=0}, @{L=3;V=2}, @{L=0;V=0}, @{L=1;V=3}, @{L=2;V=1}) },
    @{ Username = "Lautaro_Toro";     Email = "toro@test.com";          Rol = "Zona Media Alta";    Preds = @(@{L=2;V=0}, @{L=0;V=1}, @{L=1;V=1}, @{L=0;V=0}, @{L=2;V=2}) },
    @{ Username = "Dibu_ElUno";       Email = "dibu@test.com";          Rol = "Zona Media";         Preds = @(@{L=3;V=1}, @{L=1;V=0}, @{L=2;V=2}, @{L=0;V=1}, @{L=0;V=0}) },
    @{ Username = "Cuti_Romero";      Email = "cuti@test.com";          Rol = "Zona Media";         Preds = @(@{L=1;V=1}, @{L=2;V=1}, @{L=3;V=1}, @{L=0;V=2}, @{L=1;V=2}) },
    @{ Username = "MacAllister";      Email = "alexis@test.com";        Rol = "Regular";            Preds = @(@{L=1;V=0}, @{L=1;V=2}, @{L=0;V=0}, @{L=0;V=1}, @{L=1;V=1}) },
    @{ Username = "Rodri_DePaul";     Email = "rodri@test.com";         Rol = "Regular";            Preds = @(@{L=3;V=0}, @{L=0;V=2}, @{L=2;V=0}, @{L=1;V=1}, @{L=2;V=2}) },
    @{ Username = "Enzo_Fernandez";   Email = "enzo@test.com";          Rol = "Bajo Puntaje";       Preds = @(@{L=0;V=1}, @{L=1;V=1}, @{L=1;V=0}, @{L=0;V=2}, @{L=0;V=3}) },
    @{ Username = "Liberman_Mufa";    Email = "mufa@test.com";          Rol = "El Mufa (0 Puntos)"; Preds = @(@{L=0;V=3}, @{L=0;V=0}, @{L=4;V=0}, @{L=2;V=1}, @{L=1;V=0}) }
)

# 4. Registrar o autenticar a los 10 usuarios y obtener JWT tokens
Write-Host ""
Write-Host "[1/4] Registrando / Autenticando los 10 Usuarios de Prueba..." -ForegroundColor Yellow
$userTokens = @{}

foreach ($u in $usuariosDef) {
    $token = $null
    try {
        $regBody = @{
            username = $u.Username
            email = $u.Email
            password = "Password123!"
        } | ConvertTo-Json
        $res = Invoke-RestMethod -Uri "$BaseUrl/api/auth/register" -Method Post -Body $regBody -ContentType "application/json"
        $token = $res.data.token
        Write-Host "   [+] Registrado: $($u.Username) ($($u.Rol))" -ForegroundColor Green
    } catch {
        try {
            $loginBody = @{
                identifier = $u.Username
                password = "Password123!"
            } | ConvertTo-Json
            $res = Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
            $token = $res.data.token
            Write-Host "   [*] Logueado: $($u.Username) ($($u.Rol))" -ForegroundColor Cyan
        } catch {
            Write-Host "   [-] Error al autenticar $($u.Username): $_" -ForegroundColor Red
        }
    }
    if ($token) {
        $userTokens[$u.Username] = $token
    }
}

# 5. Crear 5 partidos de prueba con fecha futura
Write-Host ""
Write-Host "[2/4] Creando 5 Partidos de Prueba (fecha futura para respetar regla de 5 min)..." -ForegroundColor Yellow

$matchResults = @(
    @{ Local = 0; Vis = 1; GolesLocal = 2; GolesVis = 1; Desc = "Partido 1 (Resultado Oficial: 2 - 1)" },
    @{ Local = 1; Vis = 0; GolesLocal = 3; GolesVis = 2; Desc = "Partido 2 (Resultado Oficial: 3 - 2)" },
    @{ Local = 0; Vis = 1; GolesLocal = 1; GolesVis = 1; Desc = "Partido 3 (Resultado Oficial: 1 - 1)" },
    @{ Local = 1; Vis = 0; GolesLocal = 0; GolesVis = 2; Desc = "Partido 4 (Resultado Oficial: 0 - 2)" },
    @{ Local = 0; Vis = 1; GolesLocal = 2; GolesVis = 2; Desc = "Partido 5 (Resultado Oficial: 2 - 2)" }
)

$createdMatches = @()
$matchIndex = 0

foreach ($mr in $matchResults) {
    $matchIndex++
    $eqLocal = $equipos[$mr.Local]
    $eqVis = $equipos[$mr.Vis]
    $fechaFutura = (Get-Date).ToUniversalTime().AddDays($matchIndex + 1).ToString("yyyy-MM-ddTHH:mm:ssZ")

    $createBody = @{
        torneoId = $torneo.id
        equipoLocalId = $eqLocal.id
        equipoVisitanteId = $eqVis.id
        fechaPartido = $fechaFutura
    } | ConvertTo-Json

    $matchRes = Invoke-RestMethod -Uri "$BaseUrl/api/admin/partidos" -Method Post -Body $createBody -ContentType "application/json"
    $matchId = $matchRes.data.id
    $createdMatches += @{
        Id = $matchId
        GolesLocal = $mr.GolesLocal
        GolesVis = $mr.GolesVis
        Desc = $mr.Desc
    }
    Write-Host "   [+] Creado $matchId : $($mr.Desc)" -ForegroundColor Gray
}

# 6. Cargar los pronosticos de los 10 usuarios para cada partido
Write-Host ""
Write-Host "[3/4] Enviando pronosticos de cada usuario por la API..." -ForegroundColor Yellow

for ($mIdx = 0; $mIdx -lt $createdMatches.Count; $mIdx++) {
    $match = $createdMatches[$mIdx]
    Write-Host "   -> $($match.Desc)" -ForegroundColor DarkYellow

    foreach ($u in $usuariosDef) {
        $token = $userTokens[$u.Username]
        if (-not $token) { continue }

        $pred = $u.Preds[$mIdx]
        $predBody = @{
            partidoId = $match.Id
            golesLocal = $pred.L
            golesVisitante = $pred.V
        } | ConvertTo-Json

        $headers = @{ Authorization = "Bearer $token" }
        try {
            $pRes = Invoke-RestMethod -Uri "$BaseUrl/api/pronosticos" -Method Post -Body $predBody -ContentType "application/json" -Headers $headers
        } catch {
            Write-Host "      [-] Error en pronostico de $($u.Username): $_" -ForegroundColor DarkRed
        }
    }
}
Write-Host "   [OK] 50 pronosticos registrados (10 usuarios x 5 partidos)." -ForegroundColor Green

# 7. Iniciar partidos y liquidar resultados oficiales con el Scoring Engine
Write-Host ""
Write-Host "[4/4] Iniciando partidos y liquidando resultados oficiales..." -ForegroundColor Yellow

foreach ($m in $createdMatches) {
    # Iniciar partido -> Pasa a EN_JUEGO
    Invoke-RestMethod -Uri "$BaseUrl/api/admin/partidos/$($m.Id)/iniciar" -Method Post | Out-Null

    # Liquidar resultado -> Pasa a FINALIZADO y liquida puntos
    $settleBody = @{
        golesLocal = $m.GolesLocal
        golesVisitante = $m.GolesVis
        estado = "FINALIZADO"
    } | ConvertTo-Json

    $sRes = Invoke-RestMethod -Uri "$BaseUrl/api/admin/partidos/$($m.Id)/resultado" -Method Post -Body $settleBody -ContentType "application/json"
    Write-Host "   [OK] Liquidado: $($m.Desc) | Pronosticos: $($sRes.data.totalPronosticosLiquidados) | Puntos otorgados: $($sRes.data.totalPuntosOtorgados)" -ForegroundColor Green
}

# 8. Obtener y mostrar la Tabla de Posiciones
Write-Host ""
Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host "  TABLA DE POSICIONES OFICIAL (RANKING GENERAL ACTUALIZADO)       " -ForegroundColor Yellow
Write-Host "==================================================================" -ForegroundColor Cyan

$leaderboard = (Invoke-RestMethod -Uri "$BaseUrl/api/ranking/global" -Method Get).data

$tableData = foreach ($entry in $leaderboard) {
    [PSCustomObject]@{
        Posicion        = $entry.posicion
        Usuario         = $entry.username
        PuntosTotales   = $entry.puntosTotales
        Plenos3pts      = $entry.plenosExactos
        Tendencia1pt    = $entry.aciertosTendencia
        Pronosticos     = $entry.pronosticosTotales
    }
}
$tableData | Format-Table -AutoSize

Write-Host ""
Write-Host "[EXITO] Prueba de ranking completada con exito!" -ForegroundColor Green
Write-Host "-> Puedes ver la tabla completa en el Panel Web: http://localhost (Pestana 'Ranking')" -ForegroundColor Yellow
Write-Host "-> O en la App de Android (Pestana 'Ranking / Posiciones')" -ForegroundColor Yellow
Write-Host ""
