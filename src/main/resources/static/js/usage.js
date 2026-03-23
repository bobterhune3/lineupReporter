(function () {
    'use strict';

    const BALANCE_LABELS = ['9L','8L','7L','6L','5L','4L','3L','2L','1L','E','1R','2R','3R','4R','5R','6R','7R','8R','9R'];
    const POSITION_LABELS = ['C','1B','2B','3B','SS','LF','CF','RF','DH'];

    /** Same text as lineup column header (e.g. "L 9L-3L (76)"). */
    function lineupHeaderTextFromLineup(lu) {
        if (!lu) return '';
        return (lu.pitcherArm || '') + ' ' + (lu.balanceFrom || '') + '-' + (lu.balanceTo || '') + ' (' + (lu.estimatedAtBats || 0) + ')';
    }

    /** First letter of header: L = vs lefties, R = vs righties. */
    function pitcherGroupLetter(lu) {
        const c = lineupHeaderTextFromLineup(lu).trim().charAt(0);
        return c ? c.toUpperCase() : '';
    }

    function isPitcherArmLFromHeaderText(lu) {
        return pitcherGroupLetter(lu) === 'L';
    }

    /**
     * First column in grid order for each group (L or R) is primary; others in that group are secondary.
     * Secondary cells whose selected playerId differs from the primary column for that row get class differs-from-primary.
     */
    function updateGroupPrimaryCompareClasses() {
        const container = document.getElementById('lineup-grid-container');
        if (!container || !lineups.length) return;

        let primaryL = -1;
        let primaryR = -1;
        for (let i = 0; i < lineups.length; i++) {
            const g = pitcherGroupLetter(lineups[i]);
            if (g === 'L' && primaryL < 0) primaryL = i;
            if (g === 'R' && primaryR < 0) primaryR = i;
        }

        container.querySelectorAll('.lineup-grid-table select').forEach(function(sel) {
            sel.classList.remove('differs-from-primary');
        });

        for (let col = 0; col < lineups.length; col++) {
            const g = pitcherGroupLetter(lineups[col]);
            const primaryCol = g === 'L' ? primaryL : g === 'R' ? primaryR : -1;
            if (primaryCol < 0 || col === primaryCol) continue;

            for (let pos = 0; pos < POSITION_LABELS.length; pos++) {
                const sec = container.querySelector(
                    'select[data-lineup-index="' + col + '"][data-position-index="' + pos + '"]'
                );
                const prim = container.querySelector(
                    'select[data-lineup-index="' + primaryCol + '"][data-position-index="' + pos + '"]'
                );
                if (!sec || !prim) continue;
                const a = (sec.value || '').trim();
                const b = (prim.value || '').trim();
                if (a !== b) sec.classList.add('differs-from-primary');
            }
        }
    }

    let currentTeam = null;
    let lineups = [];
    let gridAssignments = [];
    let eligiblePlayers = {};
    let editingLineupIndex = -1;

    function api(path, options) {
        return fetch(path, Object.assign({ headers: { 'Accept': 'application/json', 'Content-Type': 'application/json' } }, options))
            .then(r => { if (!r.ok) throw new Error(r.statusText); return r.json(); });
    }

    function apiPut(path, body) {
        return fetch(path, { method: 'PUT', headers: { 'Accept': 'application/json', 'Content-Type': 'application/json' }, body: JSON.stringify(body) })
            .then(r => { if (!r.ok) throw new Error(r.statusText); });
    }

    function show(el) { el.style.display = ''; }
    function hide(el) { el.style.display = 'none'; }

    function init() {
        api('/api/usage/needs-opponents').then(needs => {
            if (needs) { window.location.href = '/opponents'; return; }
            return api('/api/usage/teams');
        }).then(teams => {
            if (!teams || !teams.length) return;
            show(document.getElementById('usage-app'));
            hide(document.getElementById('usage-not-initialized'));
            const sel = document.getElementById('team-select');
            teams.forEach(t => {
                const opt = document.createElement('option');
                opt.value = t.abrv;
                opt.textContent = (t.name || '') + ' (' + t.abrv + ')';
                sel.appendChild(opt);
            });
            sel.addEventListener('change', onTeamChange);
            document.getElementById('btn-manage-lineups').addEventListener('click', openLineupModal);
            document.getElementById('btn-settings').addEventListener('click', openSettingsModal);
            document.getElementById('btn-close-lineup-modal').addEventListener('click', () => hide(document.getElementById('lineup-modal')));
            document.getElementById('btn-add-lineup').addEventListener('click', () => openLineupForm(-1));
            document.getElementById('btn-edit-lineup').addEventListener('click', () => openLineupForm(editingLineupIndex));
            document.getElementById('btn-delete-lineup').addEventListener('click', deleteSelectedLineup);
            document.getElementById('btn-save-lineup').addEventListener('click', saveLineupForm);
            document.getElementById('btn-cancel-lineup-form').addEventListener('click', () => hide(document.getElementById('lineup-form-modal')));
            document.getElementById('btn-save-settings').addEventListener('click', saveSettings);
            document.getElementById('btn-close-settings').addEventListener('click', () => hide(document.getElementById('settings-modal')));
            document.getElementById('btn-close-copy-column-modal').addEventListener('click', () => hide(document.getElementById('copy-column-modal')));
            document.getElementById('btn-copy-column-modal-text').addEventListener('click', async () => {
                const text = (document.getElementById('copy-column-modal-text').value || '').trim();
                const ok = await tryCopyTextToClipboard(text);
                if (ok) {
                    hide(document.getElementById('copy-column-modal'));
                    alert('Copied to clipboard.');
                } else {
                    alert('Copy failed. Please select and copy the text manually.');
                }
            });
            populateBalanceDropdowns();
            loadSettings();
        }).catch(() => {});
    }

    function loadSettings() {
        api('/api/usage/settings').then(s => {
            if (s.abAddition != null) document.getElementById('setting-ab-addition').value = s.abAddition;
            if (s.targetAtBats != null) document.getElementById('setting-target-ab').value = s.targetAtBats;
        }).catch(() => {});
    }

    function openSettingsModal() {
        loadSettings();
        show(document.getElementById('settings-modal'));
    }

    function saveSettings() {
        const ab = parseInt(document.getElementById('setting-ab-addition').value, 10) || 0;
        const target = parseInt(document.getElementById('setting-target-ab').value, 10) || 615;
        apiPut('/api/usage/settings', { abAddition: ab, targetAtBats: target }).then(() => {
            hide(document.getElementById('settings-modal'));
            if (currentTeam) refreshTeamData();
        }).catch(alert);
    }

    function populateBalanceDropdowns() {
        const from = document.getElementById('balance-from');
        const to = document.getElementById('balance-to');
        from.innerHTML = ''; to.innerHTML = '';
        BALANCE_LABELS.forEach((label, i) => {
            from.appendChild(new Option(label, i));
            to.appendChild(new Option(label, i));
        });
    }

    function onTeamChange() {
        const sel = document.getElementById('team-select');
        currentTeam = sel.value || null;
        if (!currentTeam) {
            document.getElementById('balance-stats-table').querySelector('tbody').innerHTML = '';
            document.getElementById('batter-info-table').querySelector('tbody').innerHTML = '';
            document.getElementById('lineup-grid-container').innerHTML = '';
            return;
        }
        refreshTeamData();
    }

    function refreshTeamData() {
        if (!currentTeam) return;
        Promise.all([
            api('/api/usage/balance-stats/' + encodeURIComponent(currentTeam)),
            api('/api/usage/lineups/' + encodeURIComponent(currentTeam)),
            api('/api/usage/batter-info/' + encodeURIComponent(currentTeam)),
            api('/api/usage/grid/' + encodeURIComponent(currentTeam)),
            api('/api/usage/eligible-players/' + encodeURIComponent(currentTeam))
        ]).then(([balanceStats, lineupsData, batterInfo, grid, eligible]) => {
            lineups = lineupsData || [];
            gridAssignments = grid || [];
            eligiblePlayers = eligible || {};
            renderBalanceStats(balanceStats);
            renderBatterInfo(batterInfo);
            renderLineupGrid();
        }).catch(alert);
    }

    function renderBalanceStats(rows) {
        const tbody = document.getElementById('balance-stats-table').querySelector('tbody');
        tbody.innerHTML = '';
        (rows || []).forEach(r => {
            const tr = document.createElement('tr');
            tr.innerHTML = '<td>' + (r.balance || '') + '</td><td>' + (r.lhIp ?? '') + '</td><td>' + (r.estLhAb ?? '') + '</td><td>' + (r.rhIp ?? '') + '</td><td>' + (r.estRhAb ?? '') + '</td>';
            tbody.appendChild(tr);
        });
    }

    function renderBatterInfo(rows) {
        const tbody = document.getElementById('batter-info-table').querySelector('tbody');
        tbody.innerHTML = '';
        (rows || []).forEach(r => {
            const tr = document.createElement('tr');
            const remainClass = (r.remaining != null && r.remaining < 0) ? ' class="negative"' : '';
            const prjAbClass = (r.projectedAb === 0 || r.projectedAb === '0') ? ' class="prj-ab-zero"' : '';
            const projAb = r.projectedAb != null ? Number(r.projectedAb) : 0;
            const remaining = r.remaining != null ? Number(r.remaining) : 0;
            const goodClass = (projAb > 0 && remaining > 30) ? ' batter-good' : '';
            const actDisplay = (r.abAdjustment != null && r.abAdjustment > 0) ? (r.actualAb + '+' + r.abAdjustment) : (r.actualAb ?? '');
            tr.innerHTML = '<td>' + (r.player || '') + '</td><td>' + (r.defense || '') + '</td><td' + prjAbClass + '>' + (r.projectedAb ?? '') + '</td><td>' + actDisplay + '</td><td' + remainClass + '>' + (r.remaining ?? '') + '</td><td>' + (r.balance || '') + '</td><td>' + (r.positions || '') + '</td>';
            if (goodClass) tr.classList.add('batter-good');
            tbody.appendChild(tr);
        });
    }

    function renderLineupGrid() {
        const container = document.getElementById('lineup-grid-container');
        container.innerHTML = '';
        if (!lineups.length) {
            container.textContent = 'No lineups. Click "Manage lineups" to add lineups.';
            return;
        }
        // For each column, find playerIds that appear 2+ times (duplicate in same lineup)
        const duplicatePlayerIdsByCol = {};
        for (let col = 0; col < lineups.length; col++) {
            const count = {};
            for (let pos = 0; pos < POSITION_LABELS.length; pos++) {
                const a = gridAssignments.find(function(x) { return x.lineupIndex === col && x.positionIndex === pos; });
                if (a && a.playerId) {
                    const pid = String(a.playerId);
                    count[pid] = (count[pid] || 0) + 1;
                }
            }
            duplicatePlayerIdsByCol[col] = new Set(Object.keys(count).filter(function(pid) { return count[pid] >= 2; }));
        }

        const table = document.createElement('table');
        table.className = 'lineup-grid-table data-table';
        const thead = document.createElement('thead');
        const headerTr = document.createElement('tr');
        headerTr.appendChild(document.createElement('th')); // blank top-left cell for position labels
        lineups.forEach((l, i) => {
            const th = document.createElement('th');
            const cell = document.createElement('div');
            cell.className = 'lineup-grid-header-cell';

            const label = document.createElement('div');
            const headerText = lineupHeaderTextFromLineup(l);
            label.textContent = headerText;
            // LHP column: first letter of header is L (e.g. "L 9L-3L (76)"); R stays default (e.g. "R 9L-3L (89)").
            if (isPitcherArmLFromHeaderText(l)) {
                th.classList.add('lineup-grid-col-vs-lhp');
            }

            const btn = document.createElement('button');
            btn.type = 'button';
            btn.className = 'lineup-grid-copy-btn';
            btn.textContent = 'Copy';
            btn.dataset.lineupIndex = String(i);
            btn.addEventListener('click', (ev) => {
                ev.preventDefault();
                ev.stopPropagation();
                copyLineupColumnToClipboard(i);
            });

            // Column number: skip position column; column 2 => #1, column 3 => #3, … (odd numbers).
            const colNum = document.createElement('span');
            colNum.className = 'lineup-grid-column-num';
            colNum.textContent = '#' + (i + 1);

            const actionsRow = document.createElement('div');
            actionsRow.className = 'lineup-grid-header-actions';
            actionsRow.appendChild(btn);
            actionsRow.appendChild(colNum);

            cell.appendChild(label);
            cell.appendChild(actionsRow);
            th.appendChild(cell);
            headerTr.appendChild(th);
        });
        thead.appendChild(headerTr);
        table.appendChild(thead);
        const tbody = document.createElement('tbody');
        for (let pos = 0; pos < POSITION_LABELS.length; pos++) {
            const tr = document.createElement('tr');
            tr.innerHTML = '<td class="pos-label">' + POSITION_LABELS[pos] + '</td>';
            const posKey = POSITION_LABELS[pos];
            const options = eligiblePlayers[posKey] || [];
            for (let col = 0; col < lineups.length; col++) {
                const td = document.createElement('td');
                if (isPitcherArmLFromHeaderText(lineups[col])) {
                    td.classList.add('lineup-grid-col-vs-lhp');
                }
                const select = document.createElement('select');
                select.dataset.lineupIndex = col;
                select.dataset.positionIndex = pos;
                const opt0 = document.createElement('option');
                opt0.value = '';
                opt0.textContent = 'NOT SET';
                select.appendChild(opt0);
                options.forEach(o => {
                    const opt = document.createElement('option');
                    opt.value = o.id;
                    opt.textContent = (o.name || '') + ' (' + (o.defRating || '') + ')';
                    select.appendChild(opt);
                });
                const assignment = gridAssignments.find(a => a.lineupIndex === col && a.positionIndex === pos);
                if (assignment && assignment.playerId) select.value = String(assignment.playerId);
                if (!select.value) select.classList.add('not-set'); else select.classList.remove('not-set');
                if (select.value && duplicatePlayerIdsByCol[col] && duplicatePlayerIdsByCol[col].has(select.value)) select.classList.add('duplicate-in-lineup'); else select.classList.remove('duplicate-in-lineup');
                select.addEventListener('change', onGridCellChange);
                td.appendChild(select);
                tr.appendChild(td);
            }
            tbody.appendChild(tr);
        }
        table.appendChild(tbody);
        container.appendChild(table);
        updateGroupPrimaryCompareClasses();
    }

    function getLineupColumnLabel(lineupIndex) {
        const l = lineups[lineupIndex];
        if (!l) return 'Lineup ' + lineupIndex;
        return lineupHeaderTextFromLineup(l);
    }

    function buildColumnCopyText(lineupIndex) {
        // 9 lines: position label from the first column + selected player name from this lineup column.
        const container = document.getElementById('lineup-grid-container');
        const lines = [];
        for (let pos = 0; pos < POSITION_LABELS.length; pos++) {
            const select = container.querySelector(
                '.lineup-grid-table select[data-lineup-index="' + lineupIndex + '"][data-position-index="' + pos + '"]'
            );
            const option = select && select.selectedOptions && select.selectedOptions[0] ? select.selectedOptions[0] : null;
            const playerText = option && option.textContent ? option.textContent.trim() : '';
            const playerNameOnly = playerText.replace(/\s*\([^)]*\)\s*$/, '').trim();
            lines.push(POSITION_LABELS[pos] + ': ' + (playerNameOnly || playerText || 'NOT SET'));
        }
        return lines.join('\n');
    }

    async function tryCopyTextToClipboard(text) {
        try {
            if (navigator.clipboard && navigator.clipboard.writeText) {
                await navigator.clipboard.writeText(text);
                return true;
            }
        } catch (e) {
            // Clipboard can fail in non-secure contexts / browser policy. Fallback to modal.
        }
        return false;
    }

    async function copyLineupColumnToClipboard(lineupIndex) {
        const text = buildColumnCopyText(lineupIndex);
        const title = 'Copy lineup: ' + getLineupColumnLabel(lineupIndex);

        const ok = await tryCopyTextToClipboard(text);
        if (ok) {
            alert('Copied ' + getLineupColumnLabel(lineupIndex));
            return;
        }

        // Fallback UI: copy/paste from a textarea.
        document.getElementById('copy-column-modal-title').textContent = title;
        const ta = document.getElementById('copy-column-modal-text');
        ta.value = text;
        show(document.getElementById('copy-column-modal'));
        ta.focus();
        ta.select();
    }

    function onGridCellChange(ev) {
        const select = ev.target;
        const lineupIndex = parseInt(select.dataset.lineupIndex, 10);
        const positionIndex = parseInt(select.dataset.positionIndex, 10);
        const playerId = select.value ? select.value.trim() : null;
        const playerName = select.selectedOptions[0] ? select.selectedOptions[0].textContent.trim() : null;
        if (select.value) select.classList.remove('not-set'); else select.classList.add('not-set');
        let a = gridAssignments.find(x => x.lineupIndex === lineupIndex && x.positionIndex === positionIndex);
        if (!a) {
            a = { lineupIndex, positionIndex, playerId: null, playerName: null };
            gridAssignments.push(a);
        }
        a.playerId = playerId;
        a.playerName = playerName;
        updateDuplicateInLineupClassForColumn(lineupIndex);
        updateGroupPrimaryCompareClasses();
        saveGrid();
    }

    function updateDuplicateInLineupClassForColumn(col) {
        const count = {};
        for (let pos = 0; pos < POSITION_LABELS.length; pos++) {
            const a = gridAssignments.find(function(x) { return x.lineupIndex === col && x.positionIndex === pos; });
            if (a && a.playerId) {
                const pid = String(a.playerId);
                count[pid] = (count[pid] || 0) + 1;
            }
        }
        const duplicateIds = new Set(Object.keys(count).filter(function(pid) { return count[pid] >= 2; }));
        const container = document.getElementById('lineup-grid-container');
        const selects = container.querySelectorAll('.lineup-grid-table select[data-lineup-index="' + col + '"]');
        selects.forEach(function(s) {
            if (s.value && duplicateIds.has(s.value)) s.classList.add('duplicate-in-lineup'); else s.classList.remove('duplicate-in-lineup');
        });
    }

    /** Build assignments array from current grid DOM so request always has correct playerId/playerName. */
    function collectAssignmentsFromGrid() {
        const container = document.getElementById('lineup-grid-container');
        const selects = container.querySelectorAll('.lineup-grid-table select');
        const assignments = [];
        for (let col = 0; col < lineups.length; col++) {
            for (let pos = 0; pos < POSITION_LABELS.length; pos++) {
                const select = Array.from(selects).find(function(s) {
                    return parseInt(s.dataset.lineupIndex, 10) === col && parseInt(s.dataset.positionIndex, 10) === pos;
                });
                if (select) {
                    const playerId = select.value ? select.value.trim() : null;
                    const playerName = select.selectedOptions[0] ? select.selectedOptions[0].textContent.trim() : null;
                    assignments.push({ lineupIndex: col, positionIndex: pos, playerId: playerId, playerName: playerName });
                }
            }
        }
        return assignments;
    }

    function saveGrid() {
        if (!currentTeam) return;
        const assignments = collectAssignmentsFromGrid();
        const payload = assignments.length > 0 ? assignments : gridAssignments;
        apiPut('/api/usage/grid/' + encodeURIComponent(currentTeam), { assignments: payload }).then(() => {
            return api('/api/usage/batter-info/' + encodeURIComponent(currentTeam));
        }).then(rows => renderBatterInfo(rows)).catch(alert);
    }

    function openLineupModal() {
        if (!currentTeam) { alert('Select a team first.'); return; }
        const list = document.getElementById('lineup-list');
        list.innerHTML = '';
        lineups.forEach((l, i) => {
            const li = document.createElement('li');
            li.textContent = (l.pitcherArm || '') + ' ' + (l.balanceFrom || '') + '-' + (l.balanceTo || '') + ' (' + (l.estimatedAtBats || 0) + ' AB)';
            li.dataset.index = i;
            li.addEventListener('click', () => {
                document.querySelectorAll('#lineup-list li.selected').forEach(el => el.classList.remove('selected'));
                li.classList.add('selected');
                editingLineupIndex = i;
                document.getElementById('btn-edit-lineup').disabled = false;
                document.getElementById('btn-delete-lineup').disabled = false;
            });
            list.appendChild(li);
        });
        editingLineupIndex = -1;
        document.getElementById('btn-edit-lineup').disabled = true;
        document.getElementById('btn-delete-lineup').disabled = true;
        show(document.getElementById('lineup-modal'));
    }

    function openLineupForm(index) {
        editingLineupIndex = index;
        document.getElementById('lineup-form-title').textContent = index >= 0 ? 'Edit lineup' : 'Add lineup';
        const from = document.getElementById('balance-from');
        const to = document.getElementById('balance-to');
        if (index >= 0 && lineups[index]) {
            const l = lineups[index];
            document.querySelector('input[name="pitcher-arm"][value="' + (l.pitcherArm || 'R') + '"]').checked = true;
            const fromVal = BALANCE_LABELS.indexOf(l.balanceFrom);
            const toVal = BALANCE_LABELS.indexOf(l.balanceTo);
            if (fromVal >= 0) from.value = fromVal;
            if (toVal >= 0) to.value = toVal;
        } else {
            document.querySelector('input[name="pitcher-arm"][value="R"]').checked = true;
            from.value = 9; to.value = 18;
        }
        show(document.getElementById('lineup-form-modal'));
    }

    function saveLineupForm() {
        const arm = document.querySelector('input[name="pitcher-arm"]:checked');
        const fromVal = parseInt(document.getElementById('balance-from').value, 10);
        const toVal = parseInt(document.getElementById('balance-to').value, 10);
        if (fromVal > toVal) { alert('From balance must be <= To balance'); return; }
        const entry = {
            id: editingLineupIndex >= 0 && lineups[editingLineupIndex] && lineups[editingLineupIndex].id ? lineups[editingLineupIndex].id : null,
            pitcherArm: arm ? arm.value : 'R',
            balanceFromValue: fromVal,
            balanceToValue: toVal
        };
        let newLineups = lineups.map(l => ({
            id: l.id,
            pitcherArm: l.pitcherArm,
            balanceFromValue: BALANCE_LABELS.indexOf(l.balanceFrom),
            balanceToValue: BALANCE_LABELS.indexOf(l.balanceTo)
        }));
        if (editingLineupIndex >= 0) {
            newLineups[editingLineupIndex] = entry;
        } else {
            newLineups.push(entry);
        }
        apiPut('/api/usage/lineups/' + encodeURIComponent(currentTeam), { lineups: newLineups }).then(() => {
            hide(document.getElementById('lineup-form-modal'));
            hide(document.getElementById('lineup-modal'));
            refreshTeamData();
        }).catch(alert);
    }

    function deleteSelectedLineup() {
        if (editingLineupIndex < 0) return;
        const newLineups = lineups.filter((_, i) => i !== editingLineupIndex).map(l => ({
            id: l.id,
            pitcherArm: l.pitcherArm,
            balanceFromValue: BALANCE_LABELS.indexOf(l.balanceFrom),
            balanceToValue: BALANCE_LABELS.indexOf(l.balanceTo)
        }));
        apiPut('/api/usage/lineups/' + encodeURIComponent(currentTeam), { lineups: newLineups }).then(() => {
            hide(document.getElementById('lineup-modal'));
            refreshTeamData();
        }).catch(alert);
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
