module.exports = function(grunt) {

  // Project configuration.
  grunt.initConfig({
  	pkg: grunt.file.readJSON('package.json'),
  	secret: grunt.file.readJSON('../../secret.json'),
  	uglify: {
  		concat: {
  			src : 'js/*.js',
  			dest : 'js/build.js'
  		}
  	},
  	sass: {
  		compile: {
  			files: {
  				'css/app.css': 'scss/app.scss'
  			}
  		}
  	},
shell: {                                // Task
        push: {                      // Target
            options: {                      // Options
            	stdout: true
            },
            command: [
            'cd ..',
            'git add -A', 
            'git commit -m \" \"',
            'git push'
            ].join('&&')
        }
    },
    // define our ssh commands
    sshexec: {
    	deploy: {
    		command: [
    		"cd <%= pkg.path %>",
    		"git pull"
    		].join('&&'),
    		options: {
    			host: '<%= secret.host %>',
    			port: 22,
    			username: '<%= secret.username %>',
    			password: '<%= secret.password %>'
    		}
    	}
    },
    php: {
        test: {
            options: {
                keepalive: true,
                open: true
            }
        }
    }

});

  // Load the plugin that provides the "uglify" task.
  grunt.loadNpmTasks('grunt-contrib-uglify');
  grunt.loadNpmTasks('grunt-contrib-sass');
  grunt.loadNpmTasks('grunt-shell');
  grunt.loadNpmTasks('grunt-ssh');
  grunt.loadNpmTasks('grunt-php');

  // Default task(s).
  grunt.registerTask('default', ['uglify:concat','sass:compile','shell:push','sshexec:deploy']);
  grunt.registerTask('local', ['php']);


};