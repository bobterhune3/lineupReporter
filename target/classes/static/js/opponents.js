(function () {
    'use strict';

    const DIVISIONS = ['', 'A', 'B', 'C', 'D', 'E', 'F'];

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
        api('/api/usage/opponents').then(data => {
            if (!data || !data.teams || !data.teams.length) return;
            show(document.getElementById('opponents-app'));
            hide(document.getElementById('opponents-not-initialized'));
            document.getElementById('in-div-count').value = data.inDivisionGameCount || 0;
            document.getElementById('out-div-count').value = data.outOfDivisionGameCount || 0;
            const tbody = document.getElementById('opponents-table').querySelector('tbody');
            tbody.innerHTML = '';
            data.teams.forEach(t => {
                const tr = document.createElement('tr');
                const tdName = document.createElement('td');
                tdName.textContent = (t.name || '') + ' (' + (t.abrv || '') + ')';
                const tdDiv = document.createElement('td');
                const sel = document.createElement('select');
                sel.dataset.abrv = t.abrv || '';
                DIVISIONS.forEach(d => {
                    const opt = document.createElement('option');
                    opt.value = d;
                    opt.textContent = d || '--';
                    if ((t.division || '') === d) opt.selected = true;
                    sel.appendChild(opt);
                });
                sel.addEventListener('change', updateSummary);
                tdDiv.appendChild(sel);
                tr.appendChild(tdName);
                tr.appendChild(tdDiv);
                tbody.appendChild(tr);
            });
            document.getElementById('in-div-count').addEventListener('input', updateSummary);
            document.getElementById('out-div-count').addEventListener('input', updateSummary);
            document.getElementById('btn-save-opponents').addEventListener('click', save);
            updateSummary();
        }).catch(() => {});
    }

    function getTeamsFromForm() {
        const teams = [];
        document.querySelectorAll('#opponents-table tbody tr').forEach(tr => {
            const select = tr.querySelector('select');
            if (!select) return;
            const nameCell = tr.cells[0];
            const abrv = select.dataset.abrv;
            const name = nameCell ? nameCell.textContent.replace(/\s*\([^)]*\)\s*$/, '').trim() : '';
            teams.push({ abrv, name, division: select.value });
        });
        return teams;
    }

    function updateSummary() {
        const inDiv = parseInt(document.getElementById('in-div-count').value, 10) || 0;
        const outDiv = parseInt(document.getElementById('out-div-count').value, 10) || 0;
        const teams = getTeamsFromForm();
        const divCount = {};
        teams.forEach(t => {
            const d = t.division || '';
            if (d) divCount[d] = (divCount[d] || 0) + 1;
        });
        let firstDiv = '';
        let totalGames = 0;
        for (const d of Object.keys(divCount)) {
            if (!firstDiv) firstDiv = d;
            const count = divCount[d];
            totalGames += (firstDiv === d ? inDiv : outDiv) * (count - (firstDiv === d ? 1 : 0));
        }
        document.getElementById('division-summary').textContent =
            firstDiv ? 'Total games vs other teams: ' + totalGames + ' (example: ' + firstDiv + ' division)' : 'Assign divisions to see total.';
    }

    function save() {
        const teams = getTeamsFromForm();
        const inDivisionGameCount = parseInt(document.getElementById('in-div-count').value, 10) || 0;
        const outOfDivisionGameCount = parseInt(document.getElementById('out-div-count').value, 10) || 0;
        apiPut('/api/usage/opponents', { teams, inDivisionGameCount, outOfDivisionGameCount }).then(() => {
            alert('Saved.');
        }).catch(e => alert('Save failed: ' + e.message));
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
