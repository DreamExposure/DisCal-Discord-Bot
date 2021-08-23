let gulp = require('gulp');
let sass = require('gulp-sass');
let header = require('gulp-header');
let cleanCSS = require('gulp-clean-css');
let rename = require("gulp-rename");
let autoprefixer = require('gulp-autoprefixer');
let del = require("del");
let pkg = require('./package.json');
let browserSync = require('browser-sync').create();

// Set the banner content
let banner = ['/*!\n',
    ' */\n',
    '\n'
].join('');

// Copy third party libraries from /node_modules into /vendor
gulp.task('vendor', gulp.series(function (done) {
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

    // FullCalendar
    /*
    gulp.src([
        './node_modules/fullcalendar/main.min.js',
        './node_modules/fullcalendar/main.min.css'
    ])
        .pipe(gulp.dest('./web/src/main/resources/static/vendor/fullcalendar'))
     */

    // ChartJS
    gulp.src([
        './node_modules/chart.js/dist/*.js'
    ])
        .pipe(gulp.dest('./web/src/main/resources/static/vendor/chart.js'));

    // DataTables
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

gulp.task('clean:all', gulp.series(function () {
    return del([
        "./web/build/gulp",
        "./web/src/main/resources/static/assets/js",
        "./web/src/main/resources/static/assets/css",
        "./web/src/main/resources/static/vendor"
    ])
}));

gulp.task('clean:build', gulp.series(function () {
    return del([
        "./web/build/gulp"
    ])
}));

// Compile SCSS
gulp.task('css:compile', gulp.series(function () {
    return gulp.src('./web/src/main/less/**/*.scss')
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
gulp.task('css:minify', gulp.series(['css:compile'], function () {
    return gulp.src([
        './web/src/main/resources/static/assets/css/*.css',
        '!./web/src/main/resources/static/assets/css/*.min.css'
    ])
        .pipe(cleanCSS())
        .pipe(rename({
            suffix: '.min'
        }))
        .pipe(gulp.dest('./web/src/main/resources/static/assets/css'));
}));

// CSS
gulp.task('css', gulp.series(['css:compile', 'css:minify']));

// Default task
gulp.task('default', gulp.series(['clean:all', 'css', 'vendor', 'clean:build']));

gulp.task('build', gulp.series(['clean:all', 'css', 'vendor', 'clean:build']));

// Configure the browserSync task
gulp.task('browserSync', gulp.series(function () {
    browserSync.init({
        server: {
            baseDir: "./web/src/main/html/"
        }
    });
}));

// Dev task
gulp.task('dev', gulp.series(['css', 'browserSync'], function () {
    gulp.watch('./web/src/main/less/**/*.scss').on('change', gulp.series['css']);
    gulp.watch('/web/src/main/html/templates/*.html').on('change', function () {
        browserSync.reload();
    });
    gulp.watch('/web/src/main/resources/static/assets/js/*.js').on('change', function () {
        browserSync.reload();
    });
}));
