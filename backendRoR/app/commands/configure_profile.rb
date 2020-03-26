class ConfigureProfile
    prepend SimpleCommand

    def initialize(headers = {}, img, name, surname, age, weight)
        @headers = headers
        @img = img
        @name = name
        @surname = surname
        @age = age
        @weight = weight
    end

    def call
        complete
    end

    private

    attr_accessor :headers

    def complete
        if check_token
            #############################
            #@user ||= User.find(decoded_auth_token[:user_id]) if decoded_auth_token        # DEBUG ONLY PLEASE UNCOMMENT IT !!!
            # DEBUG ONLY PLEASE DELETE IT !!!
            if User.exist?(email: http_auth_header)
                @user ||= User.find_by_email(http_auth_header)                              
            else
                @user ||= User.find(decoded_auth_token[:user_id]) if decoded_auth_token
            end
            #############################

            if @user
                @user.img = @img                if not @img.empty?
                @user.name = @name              if not @name.empty?
                @user.surname = @surname        if not @surname.empty?
                @user.age = @age                if (not @age.empty?) and @age.to_i > 0
                @user.weight = @weight          if (not @weight.empty?) and @weight.to_f > 0
                @user.info_completed = true
                @user.save
            else
                errors.add(:user, 'User not found')
            end
        else
        end
    end

    def user
        @user ||= User.find(decoded_auth_token[:user_id]) if decoded_auth_token
        @user || errors.add(:token, 'Invalid token') && nil
    end

    def check_token
        #############################
        # DEBUG ONLY PLEASE DELETE IT
        if User.find_by_email(http_auth_header)
            return true
        end
        #############################

        if decoded_auth_token.nil?
            errors.add(:token, 'Invalid token')
            return false
        end

        @decoded_token = decoded_auth_token
        if @decoded_token[:exp] < Time.now.to_i
            errors.add(:token, 'Token expired, please login again')
            return false
        elsif BlacklistedToken.exists?(token: http_auth_header)
            errors.add(:token, 'Token in blacklist, please login again')
            return false
        else
            return true
        end
    end

    def decoded_auth_token
        @decoded_auth_token ||= JsonWebToken::decode(http_auth_header)
    end
  
    def http_auth_header

        if headers['Authorization'].present?
            return headers['Authorization'].split(' ').last
        else
            errors.add(:token, 'Missing token')
        end
        nil
    end
end