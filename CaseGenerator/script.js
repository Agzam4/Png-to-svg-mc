document.addEventListener('contextmenu', e => {
	e.preventDefault();
});


let eGrid = document.getElementById('grid');
eGrid.style.width = `${eGrid.getClientRects()[0].width}px`;
eGrid.style.height = `${eGrid.getClientRects()[0].height}px`;

const wsWidth = eGrid.getClientRects()[0].width;
const wsHeight = eGrid.getClientRects()[0].height;

let workspace = Math.min(eGrid.getClientRects()[0].width, eGrid.getClientRects()[0].height);

let scale = 100;//50;

// console.log(wsWidth, wsHeight, scale);

eGrid.style.fontSize = `${10}px`;

let nodeWidth = scale*50/wsWidth;
let nodeHeight = scale*50/wsHeight;

let data = {};
let eLines = {};

let ids = 0;
for (var y = 0; y < wsHeight; y+=scale) {
	for (var x = 0; x < wsWidth; x+=scale) {
		let node = document.createElement('div');
		node.classList = 'node';
		eGrid.append(node);
		node.style.left = `${x*100/wsWidth}%`;
		node.style.top = `${y*100/wsHeight}%`;
		node.style.width = `${nodeWidth}%`;
		node.style.height = `${nodeHeight}%`;
		node.setAttribute("x", x/scale);
		node.setAttribute("y", y/scale);
		node.id = ids++;
	}
}

let nodeFrom = undefined;
let type = '';

let button = 0;

window.onmousedown = e => {
	button = e.button;
	if(nodeFrom != undefined) nodeFrom.classList.remove('selected');
	if(e.target.classList.contains('node')) {
		e.target.classList.add('selected');
		nodeFrom = e.target;
	}
}

window.onclick = e => {
	if(e.target.classList.contains('line')) {
		unlink(e.target.getAttribute('n1'), e.target.getAttribute('n2'))
		setline(e.target.getAttribute('n1'), e.target.getAttribute('n2'), null);
		eGrid.removeChild(e.target);
	}
}

window.onmousemove = e => {
	if(nodeFrom == undefined) return;
	updateMouse(e.target, false);
}

window.onmouseup = e => {
	if(nodeFrom == undefined) return;
	nodeFrom.classList.remove('selected');
	updateMouse(e.target, true);
	nodeFrom = undefined;
}

window.onkeydown = e => {
	if(e.code == 'KeyS') {
		save();
	}
	// if(e.code == 'KeyC' || e.code == 'KeyN') {
	// 	type = 'create';
	// }
	// if(e.code == 'KeyQ' || e.code == 'KeyB') {
	// 	type = '';
	// }
}

function updateMouse(target, end) {
	if(target.classList == 'node' && target != nodeFrom) {
		let x1 = parseInt(nodeFrom.getAttribute('x'));
		let y1 = parseInt(nodeFrom.getAttribute('y'));
		let x2 = parseInt(target.getAttribute('x'));
		let y2 = parseInt(target.getAttribute('y'));

		if(Math.abs(x1-x2) <= 1 && Math.abs(y1-y2) <= 1) {
			if(!haslink(nodeFrom.id, target.id)) {
				let line = document.createElement('div');
				line.classList = 'line' + (button == 0 ? '' : (button == 1) ? ' create' : ' remove');
				let angle = Math.atan2(y2-y1,x2-x1);
				line.style.rotate = `${Math.round(angle*180/Math.PI)%180}deg`;
				let lw = Math.hypot(x1-x2,y1-y2)*100*scale/wsWidth;
				let lh = nodeHeight/3;
				line.style.left = `${(x1+x2)*50*scale/wsWidth - lw/2}%`;
				line.style.top = `${(y1+y2)*50*scale/wsHeight - lh/2}%`;
				line.style.height = `${lh}%`;
				line.style.width = `${lw}%`; // *scale*10/wsHeight
				// console.log(`${x1},${y1} to ${x2},${y2}`);
				// console.log(line);
				line.setAttribute('n1', nodeFrom.id)
				line.setAttribute('n2', target.id)
				setline(nodeFrom.id, target.id, line);
				link(nodeFrom.id, target.id, button);
				eGrid.append(line);
			} else {
				getline(nodeFrom.id, target.id).classList = 'line' + (button == 0 ? '' : (button == 1) ? ' create' : ' remove');
				link(nodeFrom.id, target.id, button);
			}
		}
		nodeFrom.classList.remove('selected');
		nodeFrom = target;
		nodeFrom.classList.add('selected');
	}
}

function setline(n1, n2, line) {
	if(n2 < n1) {
		setline(n2, n1, line);
		return;
	}
	if(line == null) {
		delete eLines["l" + n1 + n2];
		delete eLines["l" + n2 + n1];
		return;
	}
	eLines["l" + n1 + n2] = line;
	eLines["l" + n2 + n1] = line;
}

function getline(n1, n2) {
	if(n2 < n1) {
		return getline(n2, n1);
	}
	return eLines["l" + n1 + n2];
}

function haslink(n1, n2) {
	n1 = 'n' + n1;
	n2 = 'n' + n2;
	if(data[n1] == undefined) return false;
	if(data[n2] == undefined) return false;
	return data[n1][n2] >= 0 || data[n2][n1] >= 0;
}

function link(n1, n2, type) {
	n1 = 'n' + n1;
	n2 = 'n' + n2;
	if(data[n1] == undefined) data[n1] = {};
	if(data[n2] == undefined) data[n2] = {};
	data[n1][n2] = type;
	data[n2][n1] = type;
}

function unlink(n1, n2) {
	n1 = 'n' + n1;
	n2 = 'n' + n2;
	if(data[n1] == undefined) data[n1] = {};
	if(data[n2] == undefined) data[n2] = {};
	delete data[n1][n2];
	delete data[n2][n1];
}



function save() {
	let save = "";

	let dx = Infinity, dy = Infinity;
	let links = [];
	let nodes = {};
	for (let fKey of Object.keys(data)) {
		let n1 = parseInt(fKey.substring(1));
		let e = document.getElementById(n1);
		nodes[fKey] = {x:parseInt(e.getAttribute('x')),y:parseInt(e.getAttribute('y'))};
		dx = Math.min(dx, nodes[fKey].x);
		dy = Math.min(dy, nodes[fKey].y);
		for (let tKey of Object.keys(data[fKey])) {
			let n2 = parseInt(tKey.substring(1));
			if(n2 > n1) continue;
			console.log(n1, n2);
			links.push({
				n1: n1,
				n2: n2,
				type: data[fKey][tKey]
			});
		}
	}

	for (let link of links) {
		if(save.length != 0) save += '\n';
		let x1 = nodes['n'+link.n1].x;
		let y1 = nodes['n'+link.n1].y;
		let x2 = nodes['n'+link.n2].x;
		let y2 = nodes['n'+link.n2].y;
		save += `${link.type} ${x1-dx} ${y1-dy} ${x2-x1} ${y2-y1}`;
	}


	let a = document.createElement("a");
	a.href = URL.createObjectURL(new Blob([save], {type: "case"}));
	a.download = "case.case";
	a.click();

	console.log(dx, dy, links, nodes);
}