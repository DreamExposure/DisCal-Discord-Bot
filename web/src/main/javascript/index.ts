import {DashboardGuildRunner} from "@/dashboard/guild";
import {EmbedCalendarRunner} from "@/embed/calendar";

function loadDashboardGuildPage(apiKey: string, apiUrl: string, userId: string) {
	let drg = new DashboardGuildRunner(apiKey, apiUrl, parseInt(userId));
	drg.startDashboardGuildPage();
}

function loadEmbedCalendar(embedKey: string, apiUrl: string) {
	let embedRunner = new EmbedCalendarRunner();
	embedRunner.init(embedKey, apiUrl);
}

const body = document.getElementById("page-top")!;
if (body.dataset.embedKey != null) {
	//This will eventually be a switch or something when we get more embed pages.
	loadEmbedCalendar(<string>body.dataset.embedKey, <string>body.dataset.apiUrl);
} else if (body.dataset.dashboard != null) {
	//Get the various data attributes we need...
	const dash = <string>body.dataset.dashboard;
	const apiKey = <string>body.dataset.apiKey;
	const apiUrl = <string>body.dataset.apiUrl;
	const userId = <string>body.dataset.userId;

	switch (dash.toUpperCase()) {
		case "GUILD":
			loadDashboardGuildPage(apiKey, apiUrl, userId);
			break;
		case "CALENDAR":
			//TODO: load dashboard calendar page
			break;
		case "EVENTS":
			//TODO: load dashboard events page
			break;
		case "ANNOUNCEMENTS":
			//TODO: load dashboard announcements page
			break;
		default:
			//No default action, if its incorrect we don't do anything.
			break;
	}
}

(function ($) {
	// Toggle the side navigation
	$("#sidebarToggle, #sidebarToggleTop").on('click', function () {
		$("body").toggleClass("sidebar-toggled");
		let sidebar: any = $('.sidebar');
		sidebar.toggleClass("toggled");
		if (sidebar.hasClass("toggled")) {
			(<any>$('.sidebar .collapse')).collapse(true);
		}
	});

	// Close any open menu accordions when window is resized below 768px
	$(window).resize(function () {
		if ($(window).width()! < 768) {
			(<any>$('.sidebar .collapse')).collapse(true);
		}
	});

	// Prevent the content wrapper from scrolling when the fixed side navigation hovered over
	$('body.fixed-nav .sidebar').on('mousewheel DOMMouseScroll wheel', function (e) {
		if ($(window).width()! > 768) {
			const e0: any = e.originalEvent,
				delta = e0.wheelDelta || -e0.detail;
			this.scrollTop += (delta < 0 ? 1 : -1) * 30;
			e.preventDefault();
		}
	});

	// Scroll to top button appear
	$(document).on('scroll', function () {
		const scrollDistance = $(this).scrollTop();
		if (scrollDistance! > 100) {
			$('.scroll-to-top').fadeIn();
		} else {
			$('.scroll-to-top').fadeOut();
		}
	});

	// Smooth scrolling using jQuery easing
	$(document).on('click', 'a.scroll-to-top', function (e) {
		const $anchor: any = $(this);
		$('html, body').stop().animate({
			scrollTop: $($anchor.attr('href'))!.offset()!.top
		}, 1000, 'easeInOutExpo');
		e.preventDefault();
	});

})(jQuery);