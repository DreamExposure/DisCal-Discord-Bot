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

document.onload = function () {
	const body = document.getElementById("page-top");
	if (body!.dataset.embed_key != null) {
		loadEmbedCalendar(<string>body!.dataset.embed_key, <string>body!.dataset.api_url);
	} else if (body!.dataset.api_key != null) {
		loadDashboardGuildPage(<string>body!.dataset.api_key,
			<string>body!.dataset.api_url, <string>body!.dataset.user_id)
	}
};

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