#! /bin/bash
basedir=$(dirname "$0")
source "$basedir/echo-it.sh"
cd "$basedir/.." || exit

run_or_continue_if_fail() {
    if ! "$@"; then
        echo "❌ Failed to run $*" >&2
    fi
}

run_or_die() {
    if ! "$@"; then
        echo "❌ Failed to install ${application_display_name} while running: $*" >&2
        exit 1
    fi
}

maven_property() {
  mvn help:evaluate -Dexpression="$1" -q -DforceStdout
}

application_name=$(maven_property application.name)
application_display_name=$(maven_property application.display.name)
application_icon_name=$(maven_property application.icon.name)
application_version=$(maven_property project.version)

show-info "Building ${application_display_name}..."
run_or_die mvn clean install -Pinstaller

show-info "Installing ${application_display_name}..."
if [ "$(uname -s)" = "Linux" ]; then
  application_deb_file="target/${application_name}_${application_version}.deb"
  application_rpm_file="target/${application_name}_${application_version}.rpm"
  application_desktop_file="target/assets/${application_name}.desktop"
  target_desktop_file="$HOME/.local/share/applications/${application_name}.desktop"

  if [ "$(grep -Ei 'debian|ubuntu|mint' /etc/os-release)" ]; then
    run_or_die sudo dpkg -i ${application_deb_file}
  fi

  if [ "$(grep -Ei 'fedora|redhat' /etc/os-release)" ]; then
    run_or_die sudo dnf install ${application_rpm_file}
  fi

  run_or_die cp ${application_desktop_file} ${target_desktop_file}
  run_or_die chmod +x ${target_desktop_file}
  run_or_die sudo update-desktop-database

elif [ "$OS" = "Windows_NT" ]; then
  application_msi_file="target/${application_name}_${application_version}.msi"
  run_or_die start "" "${application_msi_file}"
fi

show-info "${application_display_name} was installed"
