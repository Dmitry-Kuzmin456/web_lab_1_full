const rButtons = document.querySelectorAll('input[name="r"]');
let selectedR = null;

function drawPoint(x, y, r, hit) {
    const canvas = document.getElementById('graphCanvas');
    const ctx = canvas.getContext('2d');
    const size = 400;
    const padding = 40;
    const graphSize = size - 2 * padding;
    const scale = graphSize / (2 * r);

    const canvasX = size / 2 + x * scale;
    const canvasY = size / 2 - y * scale;

    ctx.beginPath();
    ctx.arc(canvasX, canvasY, 5, 0, 2 * Math.PI);
    ctx.fillStyle = hit ? "green" : "red";
    ctx.fill();
}

function drawGraphWithZones(r) {
    const canvas = document.getElementById('graphCanvas');
    const ctx = canvas.getContext('2d');
    const size = 400;
    const padding = 40;
    const graphSize = size - 2 * padding;
    const scale = graphSize / (2 * r);

    ctx.clearRect(0, 0, size, size);

    ctx.fillStyle = '#ffffff';
    ctx.fillRect(0, 0, size, size);

    ctx.fillStyle = 'rgba(0, 100, 255, 0.6)';

    // треугольник
    ctx.beginPath();
    ctx.moveTo(size / 2, size / 2);
    ctx.lineTo(size / 2 - r * scale, size / 2);
    ctx.lineTo(size / 2, size / 2 - r * scale);
    ctx.closePath();
    ctx.fill();

    // прямоугольник
    ctx.beginPath();
    ctx.rect(size / 2 - r * scale, size / 2, r * scale, r * scale);
    ctx.fill();

    // круг
    ctx.beginPath();
    ctx.moveTo(size / 2, size / 2);
    ctx.arc(size / 2, size / 2, r * scale, 0, Math.PI / 2, false);
    ctx.closePath();
    ctx.fill();

    ctx.strokeStyle = '#000000';
    ctx.lineWidth = 2;
    const axisExtend = 20;

    // оси
    ctx.beginPath();
    ctx.moveTo(padding - axisExtend, size / 2);
    ctx.lineTo(size - padding + axisExtend, size / 2);
    ctx.stroke();
    ctx.beginPath();
    ctx.moveTo(size / 2, padding - axisExtend);
    ctx.lineTo(size / 2, size - padding + axisExtend);
    ctx.stroke();

    // стрелки осей
    ctx.beginPath();
    ctx.moveTo(size - padding + axisExtend, size / 2);
    ctx.lineTo(size - padding + axisExtend - 10, size / 2 - 5);
    ctx.lineTo(size - padding + axisExtend - 10, size / 2 + 5);
    ctx.closePath();
    ctx.fillStyle = '#000000';
    ctx.fill();

    ctx.beginPath();
    ctx.moveTo(size / 2, padding - axisExtend);
    ctx.lineTo(size / 2 - 5, padding - axisExtend + 10);
    ctx.lineTo(size / 2 + 5, padding - axisExtend + 10);
    ctx.closePath();
    ctx.fill();

    ctx.font = '14px Arial';
    ctx.fillStyle = '#000000';
    ctx.fillText('x', size - padding + axisExtend + 10, size / 2 - 10);
    ctx.fillText('y', size / 2 + 10, padding - axisExtend - 5);

    const marks = [-r, -r/2, r/2, r];

    marks.forEach(val => {
        const x = size / 2 + val * scale;
        ctx.beginPath();
        ctx.moveTo(x, size / 2 - 5);
        ctx.lineTo(x, size / 2 + 5);
        ctx.stroke();
        ctx.fillText(val === -r ? "-R" : val === -r/2 ? "-R/2" : val === r/2 ? "R/2" : "R", x - 12, size / 2 + 20);
    });

    marks.forEach(val => {
        const y = size / 2 - val * scale;
        ctx.beginPath();
        ctx.moveTo(size / 2 - 5, y);
        ctx.lineTo(size / 2 + 5, y);
        ctx.stroke();
        ctx.fillText(val === -r ? "-R" : val === -r/2 ? "-R/2" : val === r/2 ? "R/2" : "R", size / 2 - 35, y + 5);
    });
}

function updateTableAndGraph(history) {
    const tbody = document.getElementById("resultsTable").querySelector("tbody");
    const existingRows = tbody.rows.length;

    for (let i = existingRows; i < history.length; i++) {
        const item = history[i];
        const row = tbody.insertRow();
        row.innerHTML = `
            <td>${item.x}</td>
            <td>${item.y}</td>
            <td>${item.r}</td>
            <td>${item.result}</td>
            <td>${item.executionTime}</td>
            <td>${item.currentTime}</td>
        `;
    }

    if (history.length > 0) {
        const last = history[history.length - 1];
        drawGraphWithZones(last.r);
        drawPoint(Number(last.x), Number(last.y), last.r, last.result);
    }
}

rButtons.forEach(button => {
    button.addEventListener('click', () => {
        rButtons.forEach(b => b.classList.remove('active'));
        button.classList.add('active');
        selectedR = Number(button.value);
        drawGraphWithZones(selectedR);
    });
});

document.getElementById("pointForm").addEventListener("submit", async (e) => {
    e.preventDefault();

    const errorX = document.getElementById("errorX");
    const errorY = document.getElementById("errorY");
    const errorR = document.getElementById("errorR");
    const serverError = document.getElementById("serverError");

    [errorX, errorY, errorR].forEach(el => {
        el.textContent = "";
        el.style.display = "none";
    });
    serverError.textContent = "";
    serverError.classList.remove("visible");

    const formData = new FormData(e.target);
    const xRaw = formData.get("x");
    const yRaw = formData.get("y");
    const x = xRaw ? xRaw.trim() : "";
    const yInput = yRaw ? yRaw.trim() : "";
    const r = selectedR;

    let hasError = false;

    if (!x) {
        errorX.textContent = "Выберите X";
        errorX.style.display = "inline-block";
        hasError = true;
    }

    if (!yInput || !/^[-+]?\d+(\.\d+)?$/.test(yInput) || Number(yInput) <= -5 || Number(yInput) >= 5) {
        errorY.textContent = "Введите корректное число Y (-5...5)";
        errorY.style.display = "inline-block";
        hasError = true;
    }

    if (r === null || r === undefined) {
        errorR.textContent = "Выберите R";
        errorR.style.display = "inline-block";
        hasError = true;
    }

    if (hasError) return;

    try {
        const params = new URLSearchParams({ x, y: yInput, r });
        const response = await fetch("/calculate?" + params.toString());
        const history = await response.json();

        if (!response.ok) {
            const messages = [];
            for (const field in history) {
                if (history[field] !== "ok") messages.push(`${field.toUpperCase()}: ${history[field]}`);
            }
            serverError.innerHTML = messages.length > 0
                ? `<b>Ошибки на сервере:</b><br>${messages.join("<br>")}`
                : "Ошибка на сервере";
            serverError.classList.add("visible");
            return;
        }

        updateTableAndGraph(history);
    } catch (err) {
        serverError.textContent = "Ошибка при обращении к серверу: " + err;
        serverError.classList.add("visible");
    }
});

window.addEventListener('load', async () => {
    drawGraphWithZones(2);

    try {
        const params = new URLSearchParams({ action: "history" });
        const response = await fetch("/calculate?" + params.toString());
        const history = await response.json();
        updateTableAndGraph(history);
    } catch (err) {
        alert("Ошибка при загрузке истории: " + err);
    }
});
