$BASE = "http://localhost:8080/api/v1"
$pass = 0
$fail = 0

function Test-Endpoint {
    param(
        [string]$Name,
        [string]$Method,
        [string]$Url,
        [string]$Body,
        [int]$ExpectedStatus,
        [string]$ExpectedFragment
    )

    try {
        $status  = 0
        $content = ""

        try {
            $headers = @{ "Content-Type" = "application/json" }
            $params = @{ Method = $Method; Uri = $Url; Headers = $headers }
            if ($Body) { $params["Body"] = $Body }

            $response = Invoke-WebRequest @params -UseBasicParsing
            $status   = $response.StatusCode
            $content  = $response.Content
        } catch [System.Net.WebException] {
            $webResponse = $_.Exception.Response
            if ($webResponse) {
                $status = [int]$webResponse.StatusCode
                $stream = $webResponse.GetResponseStream()
                if ($stream) {
                    $reader = New-Object System.IO.StreamReader($stream)
                    $content = $reader.ReadToEnd()
                    $reader.Close()
                }
            } else {
                throw $_
            }
        }

        $statusOk   = ($status -eq $ExpectedStatus)
        $fragmentOk = $true
        if ($ExpectedFragment -and $ExpectedFragment.Length -gt 0) {
            $fragmentOk = $content -like "*$ExpectedFragment*"
        }

        if ($statusOk -and $fragmentOk) {
            Write-Host "  [PASS] $Name  -- HTTP $status" -ForegroundColor Green
            $script:pass++
        } else {
            Write-Host "  [FAIL] $Name" -ForegroundColor Red
            Write-Host "         Expected HTTP $ExpectedStatus  Got: $status" -ForegroundColor Yellow
            if ($ExpectedFragment -and $ExpectedFragment.Length -gt 0 -and -not $fragmentOk) {
                Write-Host "         Missing: $ExpectedFragment" -ForegroundColor Yellow
                Write-Host "         Body: $content" -ForegroundColor Gray
            }
            $script:fail++
        }
    } catch {
        Write-Host "  [ERR] $Name -- $_" -ForegroundColor Red
        $script:fail++
    }
}

Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Smart Campus API - Full Endpoint Tests    " -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan

# ── PART 1: DISCOVERY ─────────────────────────────────────────
Write-Host ""
Write-Host "PART 1 - Discovery" -ForegroundColor Magenta

Test-Endpoint "1.1 GET /api/v1 - returns HATEOAS links" `
    "GET" "$BASE" "" 200 "_links"

Test-Endpoint "1.2 GET /api/v1 - returns version" `
    "GET" "$BASE" "" 200 "version"

Test-Endpoint "1.3 GET /api/v1 - returns resource list" `
    "GET" "$BASE" "" 200 "resources"

# ── PART 2: ROOM MANAGEMENT ──────────────────────────────────
Write-Host ""
Write-Host "PART 2 - Room Management" -ForegroundColor Magenta

Test-Endpoint "2.1 POST /rooms - create LIB-301 => 201" `
    "POST" "$BASE/rooms" '{"id":"LIB-301","name":"Library Quiet Study","capacity":50}' 201 "LIB-301"

Test-Endpoint "2.2 POST /rooms - create LAB-101 => 201" `
    "POST" "$BASE/rooms" '{"id":"LAB-101","name":"Computer Lab","capacity":30}' 201 "LAB-101"

Test-Endpoint "2.3 GET /rooms - list all rooms => 200" `
    "GET" "$BASE/rooms" "" 200 "LIB-301"

Test-Endpoint "2.4 GET /rooms/LIB-301 - get by ID => 200" `
    "GET" "$BASE/rooms/LIB-301" "" 200 "Library Quiet Study"

Test-Endpoint "2.5 GET /rooms/INVALID - not found => 404" `
    "GET" "$BASE/rooms/INVALID" "" 404 ""

Test-Endpoint "2.6 DELETE /rooms/LAB-101 - delete empty room => 204" `
    "DELETE" "$BASE/rooms/LAB-101" "" 204 ""

Test-Endpoint "2.7 DELETE /rooms/GONE - non-existent => 404" `
    "DELETE" "$BASE/rooms/GONE" "" 404 ""

# ── PART 3: SENSOR MANAGEMENT ────────────────────────────────
Write-Host ""
Write-Host "PART 3 - Sensor Management" -ForegroundColor Magenta

Test-Endpoint "3.1 POST /sensors - create TEMP-001 => 201" `
    "POST" "$BASE/sensors" '{"id":"TEMP-001","type":"Temperature","status":"ACTIVE","currentValue":22.5,"roomId":"LIB-301"}' 201 "TEMP-001"

Test-Endpoint "3.2 POST /sensors - create HUM-001 => 201" `
    "POST" "$BASE/sensors" '{"id":"HUM-001","type":"Humidity","status":"ACTIVE","currentValue":60.0,"roomId":"LIB-301"}' 201 "HUM-001"

Test-Endpoint "3.3 POST /sensors - create MAINT-001 maintenance => 201" `
    "POST" "$BASE/sensors" '{"id":"MAINT-001","type":"Temperature","status":"MAINTENANCE","currentValue":0.0,"roomId":"LIB-301"}' 201 "MAINT-001"

Test-Endpoint "3.4 GET /sensors - list all => 200" `
    "GET" "$BASE/sensors" "" 200 "TEMP-001"

Test-Endpoint "3.5 GET /sensors?type=Temperature - filter => 200" `
    "GET" "$BASE/sensors?type=Temperature" "" 200 "TEMP-001"

Test-Endpoint "3.6 GET /sensors?type=Humidity - filter => 200" `
    "GET" "$BASE/sensors?type=Humidity" "" 200 "HUM-001"

Test-Endpoint "3.7 GET /sensors/TEMP-001 - get by ID => 200" `
    "GET" "$BASE/sensors/TEMP-001" "" 200 "Temperature"

Test-Endpoint "3.8 GET /sensors/INVALID - not found => 404" `
    "GET" "$BASE/sensors/INVALID" "" 404 ""

# ── PART 3.1: SENSOR VALIDATION 422 ──────────────────────────
Write-Host ""
Write-Host "PART 3.1 - Sensor Validation - 422" -ForegroundColor Magenta

Test-Endpoint "3.9  POST /sensors - invalid roomId => 422" `
    "POST" "$BASE/sensors" '{"id":"TEMP-999","type":"Temperature","status":"ACTIVE","currentValue":0.0,"roomId":"NONEXISTENT"}' 422 "Unprocessable"

Test-Endpoint "3.10 POST /sensors - 422 structured error JSON" `
    "POST" "$BASE/sensors" '{"id":"TEMP-888","type":"CO2","status":"ACTIVE","currentValue":0.0,"roomId":"FAKE"}' 422 "status"

# ── PART 4: SUB-RESOURCE READINGS ─────────────────────────────
Write-Host ""
Write-Host "PART 4 - Sub-Resource Readings" -ForegroundColor Magenta

Test-Endpoint "4.1 GET /sensors/TEMP-001/readings - initially empty => 200" `
    "GET" "$BASE/sensors/TEMP-001/readings" "" 200 ""

Test-Endpoint "4.2 POST /sensors/TEMP-001/readings - add reading => 201" `
    "POST" "$BASE/sensors/TEMP-001/readings" '{"value":23.7}' 201 "23.7"

Test-Endpoint "4.3 POST /sensors/TEMP-001/readings - 2nd reading => 201" `
    "POST" "$BASE/sensors/TEMP-001/readings" '{"value":24.1}' 201 "24.1"

Test-Endpoint "4.4 GET /sensors/TEMP-001/readings - has readings => 200" `
    "GET" "$BASE/sensors/TEMP-001/readings" "" 200 "23.7"

Test-Endpoint "4.5 GET /sensors/INVALID/readings - sensor 404" `
    "GET" "$BASE/sensors/INVALID/readings" "" 404 ""

# ── PART 5: ERROR HANDLING ────────────────────────────────────
Write-Host ""
Write-Host "PART 5 - Error Handling" -ForegroundColor Magenta

Test-Endpoint "5.1 DELETE /rooms/LIB-301 - has sensors => 409 Conflict" `
    "DELETE" "$BASE/rooms/LIB-301" "" 409 "Conflict"

Test-Endpoint "5.2 DELETE /rooms/LIB-301 - 409 structured JSON body" `
    "DELETE" "$BASE/rooms/LIB-301" "" 409 "message"

Test-Endpoint "5.3 POST /sensors/MAINT-001/readings - maintenance => 403" `
    "POST" "$BASE/sensors/MAINT-001/readings" '{"value":99.9}' 403 "Forbidden"

Test-Endpoint "5.4 POST /sensors/MAINT-001/readings - 403 structured body" `
    "POST" "$BASE/sensors/MAINT-001/readings" '{"value":99.9}' 403 "maintenance"

# ── SUMMARY ───────────────────────────────────────────────────
Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
$total = $script:pass + $script:fail
if ($script:fail -eq 0) {
    Write-Host "  ALL PASSED: $script:pass / $total" -ForegroundColor Green
} else {
    Write-Host "  PASSED: $script:pass  FAILED: $script:fail  TOTAL: $total" -ForegroundColor Yellow
}
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""
