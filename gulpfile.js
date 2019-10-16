let gulp = require('gulp');
let sass = require('gulp-sass');
let header = require('gulp-header');
let cleanDest = require('gulp-clean-dest');
let cleanCSS = require('gulp-clean-css');
let rename = require("gulp-rename");
let autoprefixer = require('gulp-autoprefixer');
let minify = require("gulp-minify");
let pkg = require('./package.json');
let browserSync = require('browser-sync').create();

// Set the banner content
let banner = ['/*!\n',
	' */\n',
	'\n'
].join('');

// Copy third party libraries from /node_modules into /vendor
gulp.task('vendor', gulp.series(function(done) {
	// Bootstrap
	gulp.src([
		'./node_modules/bootstrap/dist/**/*',
		'!./node_modules/bootstrap/dist/css/bootstrap-grid*',
		'!./node_modules/bootstrap/dist/css/bootstrap-reboot*'
	])
		.pipe(gulp.dest('./web/src/main/resources/static/vendor/bootstrap'));

	// jQuery
	gulp.src([
		'./node_modules/jquery/dist/*',
		'!./node_modules/jquery/dist/core.js'
	])
		.pipe(gulp.dest('./web/src/main/resources/static/vendor/jquery'));

	// jQuery Easing
	gulp.src([
		'./node_modules/jquery.easing/*.js'
	])
		.pipe(gulp.dest('./web/src/main/resources/static/vendor/jquery-easing'));

	//ChartJS
	gulp.src([
		'./node_modules/chart.js/dist/*.js'
	])
		.pipe(gulp.dest('./web/src/main/resources/static/vendor/chart.js'));

	//DataTables
	gulp.src([
		'./node_modules/datatables.net/js/*.js',
		'./node_modules/datatables.net-bs4/js/*.js',
		'./node_modules/datatables.net-bs4/css/*.css'
	])
		.pipe(gulp.dest('./web/src/main/resources/static/vendor/datatables'));

	// Font Awesome 5
	gulp.src([
		'./node_modules/@fortawesome/**/*'
	])
		.pipe(gulp.dest('./web/src/main/resources/static/vendor'));

	// Simple Line Icons
	gulp.src([
		'./node_modules/simple-line-icons/fonts/**'
	])
		.pipe(gulp.dest('./web/src/main/resources/static/vendor/simple-line-icons/fonts'));

	gulp.src([
		'./node_modules/simple-line-icons/css/**'
	])
		.pipe(gulp.dest('./web/src/main/resources/static/vendor/simple-line-icons/css'));

	done();
}));

// Compile SCSS
gulp.task('css:compile', gulp.series(function() {
	return gulp.src('./web/src/main/resources/src/scss/**/*.scss')
		.pipe(sass.sync({
			outputStyle: 'expanded'
		}).on('error', sass.logError))
		.pipe(autoprefixer({
			overrideBrowserslist: ['last 2 versions'],
			cascade: false
		}))
		.pipe(header(banner, {
			pkg: pkg
		}))
		.pipe(gulp.dest('./web/src/main/resources/static/assets/css'))
}));

// Minify CSS
gulp.task('css:minify', gulp.series(['css:compile'], function() {
	return gulp.src([
		'./web/src/main/resources/static/assets/css/*.css',
		'!./web/src/main/resources/static/assets/css/*.min.css'
	])
		.pipe(cleanCSS())
		.pipe(rename({
			suffix: '.min'
		}))
		.pipe(gulp.dest('./web/src/main/resources/static/assets/css'))
		.pipe(browserSync.stream());
}));

//Minify JS
gulp.task('js:minify', gulp.series(function() {
	return gulp.src([
		'./web/src/main/resources/src/js/**/*.js',
		'!./web/src/main/resources/src/js/**/*.min.js'
	])
		.pipe(cleanDest('./web/src/main/resources/static/assets/js'))
		.pipe(minify({
			noSource: true,
			ext: {
				min: '.min.js'
			}
		}))
		.pipe(gulp.dest('./web/src/main/resources/static/assets/js'))
		.pipe(browserSync.stream());
}));

// CSS
gulp.task('css', gulp.series(['css:compile', 'css:minify']));

// JS
gulp.task('js', gulp.series(['js:minify']));

// Default task
gulp.task('default', gulp.series(['css', 'js', 'vendor']));

gulp.task('build', gulp.series(['css', 'js']));

// Configure the browserSync task
gulp.task('browserSync', gulp.series(function() {
	browserSync.init({
		server: {
			baseDir: "./web/src/main/resources/"
		}
	});
}));

// Dev task
gulp.task('dev', gulp.series(['css', 'js', 'browserSync'], function() {
	gulp.watch('./web/src/main/resources/src/scss/*.scss').on('change', gulp.series['css']);
	gulp.watch('/web/src/main/resources/templates/*.html').on('change', function() {
		browserSync.reload();
	});
	gulp.watch('/web/src/main/resources/static/assets/js/*.js').on('change', function() {
		browserSync.reload();
	});
}));