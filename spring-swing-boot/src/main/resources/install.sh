#! /bin/bash
basedir=$(dirname "$0")
source "$basedir/echo-it.sh"
cd "$basedir/.." || exit

run_or_continue_if_fail() {
    if ! "$@"; then
        show-error "❌ Failed to run $*"
    fi
}

run_or_die() {
    if ! "$@"; then
        show-error "❌ Failed to install ${application_display_name} while running: $*"
        exit 1
    fi
}

maven_property() {
  mvn help:evaluate -Dexpression="$1" -q -DforceStdout
}

is_available() {
  command -v "$1" >/dev/null 2>&1
}

install() {
  show-info "Installing $1 ..."
  local installed=false;
  if $is_linux; then
    if is_available apt-get; then
      run_or_continue_if_fail sudo apt-get update
      run_or_continue_if_fail sudo apt-get install -y "$1"
      installed=true;
    elif is_available dnf; then
      run_or_continue_if_fail sudo dnf install -y "$1"
      installed=true;
    elif is_available yum; then
      run_or_continue_if_fail sudo yum install -y "$1"
      installed=true;
    elif is_available pacman; then
      run_or_continue_if_fail sudo pacman -Sy --noconfirm "$1"
      installed=true;
    elif is_available apk; then
      run_or_continue_if_fail sudo apk add "$1"
      installed=true;
    fi
  fi

  if ! $installed; then
    show-error "❌ Failed to install '$1'."
  fi
}

is_linux=false
[ "$(uname -s)" = "Linux" ] && is_linux=true

is_windows=false
[ "$OS" = "Windows_NT" ] && is_windows=true

is_debian=false
[ "$is_linux" = true ] && [ "$(grep -Ei 'debian|ubuntu|mint' /etc/os-release)" ] && is_debian=true

is_redhat=false
[ "$is_linux" = true ] && [ "$(grep -Ei 'fedora|redhat' /etc/os-release)" ] && is_redhat=true

application_name=$(maven_property application.name)
application_display_name=$(maven_property application.display.name)
application_icon_name=$(maven_property application.icon.name)
application_version=$(maven_property project.version)

if $is_linux; then
  if ! is_available objcopy; then
    install binutils
  fi
fi

show-info "Building ${application_display_name}..."
run_or_die mvn clean install -Pinstaller

show-info "Installing ${application_display_name}..."
if $is_linux; then
  application_deb_file="target/${application_name}_${application_version}.deb"
  application_rpm_file="target/${application_name}_${application_version}.rpm"
  application_desktop_file="target/assets/${application_name}.desktop"
  target_desktop_file="$HOME/.local/share/applications/${application_name}.desktop"

  if is_available dpkg; then
    run_or_die sudo dpkg -i ${application_deb_file}
  elif is_available dnf; then
    run_or_die sudo dnf install ${application_rpm_file}
  fi

  run_or_die cp ${application_desktop_file} ${target_desktop_file}
  run_or_die chmod +x ${target_desktop_file}
  run_or_die sudo update-desktop-database

elif $is_windows; then
  application_msi_file="target/${application_name}_${application_version}.msi"
  run_or_die start "" "${application_msi_file}"
fi

show-info "${application_display_name} was installed"
