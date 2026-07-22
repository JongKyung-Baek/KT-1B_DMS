
function imgCanvas(){

	var b1 = document.getElementById('big1');
	var b1 = b1.getContext('2d');

	b1.beginPath();
	b1.moveTo(95,361);
	b1.fillStyle = "rgba(253,162,19,0.45)";
	b1.bezierCurveTo(111,208, 290,76, 497,78);
	b1.fillStyle = "rgba(254,159,9,0.45)";
	b1.bezierCurveTo(702,74, 856,199, 840,358);
	b1.fillStyle = "rgba(255,102,0,0.45)";
	b1.bezierCurveTo(824,516, 644,648, 439,647);
	b1.fillStyle = "rgba(254,135,10,0.45)";
	b1.bezierCurveTo(232,648, 79,521, 95,361);
	b1.fill();
	b1.rotate(10);
	b1.closePath();

	var b2 = document.getElementById('big2');
	var b2 = b2.getContext('2d');
	b2.beginPath();
	b2.moveTo(169,126);
	b2.fillStyle = "rgba(234,94,0, 0.5)";
	b2.bezierCurveTo(296,6, 501,18, 627,152);
	b2.fillStyle = "rgba(252,101,0, 0.5)";
	b2.bezierCurveTo(756,285, 757,491, 634,610);
	b2.fillStyle = "rgba(253,101,0, 0.5)";
	b2.bezierCurveTo(506,728, 302,719, 175,587);
	b2.fillStyle = "rgba(235,94,0, 0.5)";
	b2.bezierCurveTo(47,452, 45,248, 169,126);
	b2.fill();
	b2.rotate(45);
	b2.closePath();

	var b3 = document.getElementById('big3');
	var b3 = b3.getContext('2d');
	b3.beginPath();
	b3.moveTo(124,119);
	b3.fillStyle = "rgba(4,4,4, 0.1)";
	b3.bezierCurveTo(241,14, 469,37, 639,171);
	b3.fillStyle = "rgba(53,53,53, 0.1)";
	b3.bezierCurveTo(808,303, 851,498, 734,601);
	b3.fillStyle = "rgba(60,60,60, 0.1)";
	b3.bezierCurveTo(619,708, 389,681, 222,550);
	b3.fillStyle = "rgba(11,11,11, 0.1)";
	b3.bezierCurveTo(50,417, 8,224, 124,119);
	b3.fill();
	b3.rotate(45);
	b3.closePath();

	var sc = document.getElementById('semiCircle');
	var sc = sc.getContext('2d');
	sc.beginPath();
	sc.fillStyle = "rgba(222,223,224, 0.5)";
	sc.moveTo(0,0);
	sc.fillStyle = "rgba(224,225,226, 0.5)";
	sc.lineTo(15,0);
	sc.fillStyle = "rgba(244,244,245, 0.5)";
	sc.bezierCurveTo(67,0, 110,43, 110,95);
	sc.lineTo(110,105);
	sc.bezierCurveTo(110,157, 67,200, 15,200);
	sc.fillStyle = "rgba(224,225,226, 0.5)";
	sc.lineTo(15,200);
	sc.fillStyle = "rgba(222,223,224, 0.5)";
	sc.lineTo(0,200);
	sc.fill();
	sc.rotate(45);
	sc.closePath();

	$('.canvasLineBar').each(function(index, element){
		var lb = element.getContext('2d');
		lb.beginPath();
		lb.scale(0.5,0.5);
		lb.fillStyle = "rgba(253,166,28, 0.15)";
		lb.moveTo(10,13);
		lb.fillStyle = "rgba(253,180,67, 0.15)";
		lb.bezierCurveTo(11,11, 14,9, 15,10);
		lb.fillStyle = "rgba(255,129,43, 0.15)";
		lb.lineTo(105,34);
		lb.fillStyle = "rgba(255,151,82, 0.15)";
		lb.bezierCurveTo(107,35, 108,36, 108,39);
		lb.fillStyle = "rgba(255,108,80, 0.15)";
		lb.bezierCurveTo(107,41, 105,43, 103,42);
		lb.fillStyle = "rgba(253,161,19, 0.15)";
		lb.lineTo(13,18);
		lb.fillStyle = "rgba(253,166,28, 0.15)";
		lb.bezierCurveTo(11,17, 10,15, 10,13);
		lb.fill();
		lb.rotate(25);
		lb.closePath();
	});

	var sh1 = document.getElementById('shape1');
	var sh1 = sh1.getContext('2d');
	sh1.beginPath();
	sh1.scale(0.25,0.25);
	sh1.fillStyle = "rgba(253,162,19, 0.45)";
	sh1.moveTo(11,79);
	sh1.fillStyle = "rgba(254,129,9, 0.45)";
	sh1.bezierCurveTo(14,41, 57,10, 107,10);
	sh1.fillStyle = "rgba(255,102,0, 0.45)";
	sh1.bezierCurveTo(157,9, 194,40, 190, 79);
	sh1.fillStyle = "rgba(254,135,11, 0.45)";
	sh1.bezierCurveTo(186,116, 142,147, 93,147);
	sh1.fillStyle = "rgba(253,162,19, 0.45)";
	sh1.bezierCurveTo(43,148, 6,118, 11,79);
	sh1.fill();
	sh1.rotate(15);
	sh1.closePath();
	
	var sh2 = document.getElementById('shape2');
	var sh2 = sh2.getContext('2d');
	sh2.beginPath();
	sh2.scale(0.35,0.35);
	sh2.fillStyle = "rgba(7,7,7, 0.25)";
	sh2.moveTo(11,79);
	sh2.fillStyle = "rgba(56,56,56, 0.25)";
	sh2.bezierCurveTo(14,41, 57,10, 107,10);
	sh2.fillStyle = "rgba(57,57,57, 0.25)";
	sh2.bezierCurveTo(157,9, 194,40, 190, 79);
	sh2.fillStyle = "rgba(8,8,8, 0.25)";
	sh2.bezierCurveTo(186,116, 142,147, 93,147);
	sh2.fillStyle = "rgba(7,7,7, 0.25)";
	sh2.bezierCurveTo(43,148, 6,118, 11,79);
	sh2.fill();
	sh2.rotate(15);
	sh2.closePath();

	var sh3 = document.getElementById('shape3');
	var sh3 = sh3.getContext('2d');
	sh3.beginPath();
	sh3.scale(0.25,0.25);
	sh3.fillStyle = "rgba(253,162,19, 0.15)";
	sh3.moveTo(33,29);
	sh3.fillStyle = "rgba(254,129,9, 0.15)";
	sh3.bezierCurveTo(63,1, 112,3, 141,36);
	sh3.fillStyle = "rgba(255,102,0, 0.15)";
	sh3.bezierCurveTo(172,68, 173,117, 142,145);
	sh3.fillStyle = "rgba(254,135,11, 0.15)";
	sh3.bezierCurveTo(112,174, 64,171, 33,139);
	sh3.fillStyle = "rgba(253,162,19, 0.15)";
	sh3.bezierCurveTo(2,107, 2,58, 33,29);
	sh3.fill();
	sh3.rotate(45);
	sh3.closePath();

	var sh4 = document.getElementById('shape4');
	var sh4 = sh4.getContext('2d');
	sh4.beginPath();
	sh4.scale(2,2);
	sh4.fillStyle = "rgba(7,7,7, 0.05)";
	sh4.moveTo(33,29);
	sh4.fillStyle = "rgba(56,56,56, 0.05)";
	sh4.bezierCurveTo(63,1, 112,3, 141,36);
	sh4.fillStyle = "rgba(57,57,57, 0.05)";
	sh4.bezierCurveTo(172,68, 173,117, 142,145);
	sh4.fillStyle = "rgba(8,8,8, 0.05)";
	sh4.bezierCurveTo(112,174, 64,171, 33,139);
	sh4.fillStyle = "rgba(7,7,7, 0.05)";
	sh4.bezierCurveTo(2,107, 2,58, 33,29);
	sh4.fill();
	sh4.rotate(45);
	sh4.closePath();

	$('.canvasShape3').each(function(index, element){
		var cs3 = element.getContext('2d');
		cs3.beginPath();
		cs3.scale(0.3,0.3);
		cs3.fillStyle = "rgba(4,4,4, 0.05)";
		cs3.moveTo(31,29);
		cs3.fillStyle = "rgba(53,53,53, 0.05)";
		cs3.bezierCurveTo(57,6, 108,11, 146,41);
		cs3.fillStyle = "rgba(60,60,60, 0.05)";
		cs3.bezierCurveTo(184,72, 193,116, 167,139);
		cs3.fillStyle = "rgba(12,12,12, 0.05)";
		cs3.bezierCurveTo(142,164, 90,159, 52,127);
		cs3.fillStyle = "rgba(4,4,4, 0.05)";
		cs3.bezierCurveTo(14,97, 4,54, 31,29);
		cs3.fill();
		cs3.rotate(45);
		cs3.closePath();
	});


}